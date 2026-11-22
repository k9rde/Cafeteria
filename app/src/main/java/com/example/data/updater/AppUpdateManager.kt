package com.example.data.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String
)

sealed interface AppUpdateState {
    data object Idle : AppUpdateState
    data object Checking : AppUpdateState
    data object UpToDate : AppUpdateState
    data class Available(val info: AppUpdateInfo) : AppUpdateState
    data class Downloading(
        val info: AppUpdateInfo,
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : AppUpdateState
    data class NeedsInstallPermission(val info: AppUpdateInfo, val apkFile: File) : AppUpdateState
    data class ReadyToInstall(val info: AppUpdateInfo, val apkFile: File) : AppUpdateState
    data class Error(val message: String, val info: AppUpdateInfo? = null) : AppUpdateState
}

class AppUpdateManager(private val context: Context) {

    companion object {
        private const val RELEASE_VERSION_URL =
            "https://github.com/k9rde/Cafeteria/releases/download/latest/version.json"
        private const val PAGES_VERSION_URL =
            "https://k9rde.github.io/Cafeteria/version.json"
        private const val DEFAULT_APK_URL =
            "https://github.com/k9rde/Cafeteria/releases/download/latest/Cafeteria-POS.apk"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var downloadJob: Job? = null
    private var autoCheckDone = false

    private val _state = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)
    val state: StateFlow<AppUpdateState> = _state.asStateFlow()

    private val _postponedUpdate = MutableStateFlow<AppUpdateInfo?>(null)
    val postponedUpdate: StateFlow<AppUpdateInfo?> = _postponedUpdate.asStateFlow()

    private val _isDialogVisible = MutableStateFlow(false)
    val isDialogVisible: StateFlow<Boolean> = _isDialogVisible.asStateFlow()

    fun cleanupOldUpdateFiles() {
        scope.launch(Dispatchers.IO) {
            runCatching {
                val updatesDir = File(context.cacheDir, "updates")
                if (updatesDir.exists()) {
                    updatesDir.listFiles()?.forEach { file ->
                        file.delete()
                    }
                }
            }
        }
    }

    fun checkForUpdates(manual: Boolean = false) {
        if (!manual && autoCheckDone) return
        if (_state.value is AppUpdateState.Checking || _state.value is AppUpdateState.Downloading) {
            if (manual) _isDialogVisible.value = true
            return
        }
        autoCheckDone = true

        scope.launch {
            if (manual) {
                _state.value = AppUpdateState.Checking
            }
            val result = withContext(Dispatchers.IO) {
                fetchLatestUpdateInfo()
            }

            result.onSuccess { remote ->
                if (remote != null && remote.versionCode > BuildConfig.VERSION_CODE) {
                    _postponedUpdate.value = remote
                    _state.value = AppUpdateState.Available(remote)
                    _isDialogVisible.value = true
                } else {
                    _postponedUpdate.value = null
                    _state.value = if (manual) AppUpdateState.UpToDate else AppUpdateState.Idle
                }
            }.onFailure { err ->
                if (manual) {
                    _state.value = AppUpdateState.Error(
                        message = "تعذر الاتصال للتحقق من التحديثات. تأكد من توفر الإنترنت ثم حاول مجدداً.",
                        info = null
                    )
                    _isDialogVisible.value = true
                }
            }
        }
    }

    fun reopenPostponedUpdate() {
        val pending = _postponedUpdate.value
        if (pending != null) {
            if (_state.value !is AppUpdateState.Downloading &&
                _state.value !is AppUpdateState.ReadyToInstall &&
                _state.value !is AppUpdateState.NeedsInstallPermission
            ) {
                _state.value = AppUpdateState.Available(pending)
            }
            _isDialogVisible.value = true
        } else {
            checkForUpdates(manual = true)
        }
    }

    fun postponeUpdate() {
        val current = _state.value
        if (current is AppUpdateState.Available) {
            _postponedUpdate.value = current.info
        } else if (current is AppUpdateState.Error && current.info != null) {
            _postponedUpdate.value = current.info
        }
        _isDialogVisible.value = false
        if (current is AppUpdateState.Error || current is AppUpdateState.UpToDate) {
            _state.value = AppUpdateState.Idle
        }
    }

    fun dismissDialogOnly() {
        _isDialogVisible.value = false
    }

    fun startUpdateDownload(info: AppUpdateInfo) {
        downloadJob?.cancel()
        downloadJob = scope.launch {
            _state.value = AppUpdateState.Downloading(
                info = info,
                progress = 0f,
                downloadedBytes = 0L,
                totalBytes = -1L
            )
            _isDialogVisible.value = true

            try {
                val apkFile = withContext(Dispatchers.IO) {
                    downloadApkWithProgress(info) { downloaded, total ->
                        val progress = if (total > 0L) {
                            (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                        } else {
                            -1f
                        }
                        _state.value = AppUpdateState.Downloading(
                            info = info,
                            progress = progress,
                            downloadedBytes = downloaded,
                            totalBytes = total
                        )
                    }
                }

                if (canInstallPackages()) {
                    _state.value = AppUpdateState.ReadyToInstall(info, apkFile)
                    launchInstaller(apkFile)
                } else {
                    _state.value = AppUpdateState.NeedsInstallPermission(info, apkFile)
                }
            } catch (_: CancellationException) {
                _state.value = AppUpdateState.Available(info)
            } catch (e: Exception) {
                _state.value = AppUpdateState.Error(
                    message = "تعذر تحميل ملف التحديث: ${e.localizedMessage ?: "خطأ في الاتصال"}",
                    info = info
                )
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        val pending = _postponedUpdate.value
        if (pending != null) {
            _state.value = AppUpdateState.Available(pending)
        } else {
            _state.value = AppUpdateState.Idle
            _isDialogVisible.value = false
        }
    }

    fun canInstallPackages(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()
    }

    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun onHostResumed() {
        val current = _state.value
        if (current is AppUpdateState.NeedsInstallPermission && canInstallPackages()) {
            _state.value = AppUpdateState.ReadyToInstall(current.info, current.apkFile)
            launchInstaller(current.apkFile)
        }
    }

    fun installDownloadedApk(apkFile: File, info: AppUpdateInfo) {
        if (!apkFile.exists()) {
            startUpdateDownload(info)
            return
        }
        if (canInstallPackages()) {
            _state.value = AppUpdateState.ReadyToInstall(info, apkFile)
            launchInstaller(apkFile)
        } else {
            _state.value = AppUpdateState.NeedsInstallPermission(info, apkFile)
        }
    }

    private fun launchInstaller(apkFile: File) {
        runCatching {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure { err ->
            val currentInfo = when (val s = _state.value) {
                is AppUpdateState.ReadyToInstall -> s.info
                is AppUpdateState.NeedsInstallPermission -> s.info
                else -> _postponedUpdate.value
            }
            _state.value = AppUpdateState.Error(
                message = "تعذر فتح مثبت النظام: ${err.localizedMessage ?: "يرجى المحاولة مرة أخرى"}",
                info = currentInfo
            )
        }
    }

    private fun fetchLatestUpdateInfo(): Result<AppUpdateInfo?> {
        val candidates = listOf(RELEASE_VERSION_URL, PAGES_VERSION_URL)
        var lastError: Exception? = null
        var bestRemote: AppUpdateInfo? = null

        for (baseUrl in candidates) {
            try {
                val urlWithCacheBust = "$baseUrl?t=${System.currentTimeMillis()}"
                val conn = openFollowingRedirects(urlWithCacheBust, readTimeoutMs = 15_000)
                try {
                    val code = conn.responseCode
                    if (code !in 200..299) {
                        lastError = IllegalStateException("HTTP $code")
                        continue
                    }
                    val body = conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    val json = JSONObject(body)
                    val remoteCode = json.optInt("versionCode", 0)
                    val remoteName = json.optString("versionName", "1.0")
                    val apkUrl = json.optString("apkUrl", DEFAULT_APK_URL)
                        .takeIf { it.startsWith("https://") } ?: DEFAULT_APK_URL
                    val notes = json.optString(
                        "releaseNotes",
                        "تحسينات في الأداء والسلاسة وإصلاحات عامة دون حذف أي بيانات."
                    )

                    if (remoteCode > 0) {
                        val parsed = AppUpdateInfo(
                            versionCode = remoteCode,
                            versionName = remoteName,
                            apkUrl = apkUrl,
                            releaseNotes = notes
                        )
                        if (bestRemote == null || parsed.versionCode > bestRemote.versionCode) {
                            bestRemote = parsed
                        }
                    }
                } finally {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                lastError = e
            }
        }

        return if (bestRemote != null) {
            Result.success(bestRemote)
        } else {
            Result.failure(lastError ?: IllegalStateException("Failed to check update"))
        }
    }

    private suspend fun downloadApkWithProgress(
        info: AppUpdateInfo,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ): File {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val tempFile = File(updatesDir, "cafeteria-update-${info.versionCode}.tmp")
        val targetFile = File(updatesDir, "cafeteria-update-${info.versionCode}.apk")
        if (tempFile.exists()) tempFile.delete()
        if (targetFile.exists()) targetFile.delete()

        val conn = openFollowingRedirects(info.apkUrl, readTimeoutMs = 35_000)
        try {
            val statusCode = conn.responseCode
            if (statusCode !in 200..299) {
                throw IllegalStateException("فشل التحميل (رمز $statusCode)")
            }

            val totalBytes = conn.contentLengthLong
            var downloadedBytes = 0L
            var lastReportedBytes = 0L

            conn.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        downloadedBytes += read
                        if (downloadedBytes - lastReportedBytes >= 64 * 1024 || downloadedBytes == totalBytes) {
                            lastReportedBytes = downloadedBytes
                            onProgress(downloadedBytes, totalBytes)
                        }
                    }
                    output.flush()
                }
            }
            onProgress(downloadedBytes, totalBytes)
        } catch (t: Throwable) {
            tempFile.delete()
            throw t
        } finally {
            conn.disconnect()
        }

        if (!isValidApkFile(tempFile)) {
            tempFile.delete()
            throw IllegalStateException("الملف المحمل غير صالح أو غير مكتمل")
        }

        if (!tempFile.renameTo(targetFile)) {
            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
        }
        return targetFile
    }

    private fun isValidApkFile(file: File): Boolean {
        if (!file.exists() || file.length() < 512_000L) return false
        return runCatching {
            file.inputStream().use { stream ->
                val header = ByteArray(2)
                val count = stream.read(header)
                count == 2 && header[0] == 'P'.code.toByte() && header[1] == 'K'.code.toByte()
            }
        }.getOrDefault(false)
    }

    private fun openFollowingRedirects(startUrl: String, readTimeoutMs: Int): HttpURLConnection {
        var currentUrl = URL(startUrl)
        repeat(6) {
            val conn = (currentUrl.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                connectTimeout = 12_000
                readTimeout = readTimeoutMs
                setRequestProperty("Accept", "*/*")
                setRequestProperty("User-Agent", "CafeteriaPOS/${BuildConfig.VERSION_NAME}")
            }
            val code = conn.responseCode
            if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                code == HttpURLConnection.HTTP_MOVED_TEMP ||
                code == HttpURLConnection.HTTP_SEE_OTHER ||
                code == 307 ||
                code == 308
            ) {
                val location = conn.getHeaderField("Location")
                conn.disconnect()
                if (location.isNullOrBlank()) {
                    throw IllegalStateException("إعادة توجيه غير صالحة من الخادم")
                }
                currentUrl = URL(currentUrl, location)
            } else {
                return conn
            }
        }
        throw IllegalStateException("تجاوز عدد التحويلات المسموح")
    }
}
