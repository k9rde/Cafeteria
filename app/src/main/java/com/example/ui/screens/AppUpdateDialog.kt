package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.InstallMobile
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.BuildConfig
import com.example.R
import com.example.data.updater.AppUpdateInfo
import com.example.data.updater.AppUpdateState
import com.example.ui.components.GlassCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusBillRed
import java.io.File
import java.util.Locale

@Composable
fun AppUpdateDialog(
    updateState: AppUpdateState,
    onStartDownload: (AppUpdateInfo) -> Unit,
    onPostpone: () -> Unit,
    onCancelDownload: () -> Unit,
    onInstallReady: (File, AppUpdateInfo) -> Unit,
    onOpenPermissionSettings: () -> Unit,
    onRetryCheck: () -> Unit
) {
    Dialog(
        onDismissRequest = {
            if (updateState !is AppUpdateState.Downloading) {
                onPostpone()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = updateState !is AppUpdateState.Downloading,
            dismissOnClickOutside = updateState !is AppUpdateState.Downloading
        )
    ) {
        GlassCard(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .testTag("app_update_dialog"),
            shape = RoundedCornerShape(30.dp),
            opacityMultiplier = 1.28f,
            borderColor = AmberGold.copy(alpha = 0.55f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo with Sparkling Update Badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Image(
                        painter = painterResource(id = R.drawable.img_qissa_logo),
                        contentDescription = "شعار قصة كافيه",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(2.dp, AmberGold.copy(alpha = 0.85f), CircleShape)
                    )
                    Surface(
                        shape = CircleShape,
                        color = AmberGold,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.SystemUpdate,
                                contentDescription = null,
                                tint = Color(0xFF1A1104),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                AnimatedContent(
                    targetState = updateState,
                    label = "update_dialog_content"
                ) { state ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (state) {
                            AppUpdateState.Checking -> {
                                Text(
                                    text = "جارٍ التحقق من التحديثات...",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                CircularProgressIndicator(color = AmberGold)
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    text = "الإصدار الحالي: ${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(16.dp))
                                TextButton(onClick = onPostpone) {
                                    Text("إغلاق")
                                }
                            }

                            AppUpdateState.UpToDate -> {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusAvailableGreen,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "تطبيقك محدّث بالكامل",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "أنت تستخدم أحدث إصدار متاح (${BuildConfig.VERSION_NAME})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(18.dp))
                                Button(
                                    onClick = onPostpone,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberGold,
                                        contentColor = Color(0xFF1A1104)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                ) {
                                    Text("حسناً")
                                }
                            }

                            is AppUpdateState.Available -> {
                                UpdateDetailsHeader(info = state.info)
                                Spacer(Modifier.height(18.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { onStartDownload(state.info) },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AmberGold,
                                            contentColor = Color(0xFF1A1104)
                                        ),
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .heightIn(min = 50.dp)
                                            .testTag("update_now_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "التحديث الآن",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = onPostpone,
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 50.dp)
                                            .testTag("update_later_button")
                                    ) {
                                        Text(
                                            text = "التحديث لاحقاً",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            is AppUpdateState.Downloading -> {
                                Text(
                                    text = "جارٍ تنزيل التحديث (${state.info.versionName})",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(6.dp))
                                DataSafetyBadge()
                                Spacer(Modifier.height(18.dp))

                                val animatedProgress by animateFloatAsState(
                                    targetValue = if (state.progress >= 0f) state.progress else 0f,
                                    animationSpec = tween(durationMillis = 180),
                                    label = "apk_download_progress"
                                )

                                // GlassWave-style Sleek Download Progress Bar
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.38f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (state.progress >= 0f) {
                                                    "${(animatedProgress * 100).toInt()}%"
                                                } else {
                                                    "جارٍ التحميل..."
                                                },
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = AmberGold
                                            )
                                            Text(
                                                text = formatTransferBytes(state.downloadedBytes, state.totalBytes),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (state.progress >= 0f) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(12.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth(animatedProgress.coerceIn(0.02f, 1f))
                                                        .height(12.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                listOf(AccentCyan, AmberGold)
                                                            )
                                                        )
                                                )
                                            }
                                        } else {
                                            LinearProgressIndicator(
                                                color = AmberGold,
                                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(10.dp)
                                                    .clip(CircleShape)
                                            )
                                        }

                                        Text(
                                            text = "يرجى الانتظار... سيتم فتح التثبيت المباشر فور اكتمال التحميل",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = onCancelDownload,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, StatusBillRed.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        tint = StatusBillRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("إلغاء التحميل", color = StatusBillRed)
                                }
                            }

                            is AppUpdateState.NeedsInstallPermission -> {
                                Icon(
                                    imageVector = Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(38.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "السماح بتثبيت التحديث",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "اكتمل تحميل التحديث بنجاح! يرجى تفعيل خيار «السماح بتثبيت التطبيقات من هذا المصدر» لإكمال التحديث مباشرة دون حذف أي بيانات.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(18.dp))
                                Button(
                                    onClick = onOpenPermissionSettings,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberGold,
                                        contentColor = Color(0xFF1A1104)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 50.dp)
                                ) {
                                    Text(
                                        text = "منح الصلاحية والمتابعة",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = onPostpone) {
                                    Text("التحديث لاحقاً")
                                }
                            }

                            is AppUpdateState.ReadyToInstall -> {
                                Icon(
                                    imageVector = Icons.Rounded.InstallMobile,
                                    contentDescription = null,
                                    tint = StatusAvailableGreen,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "التحديث جاهز للتثبيت",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(6.dp))
                                DataSafetyBadge()
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "تم تحميل الإصدار ${state.info.versionName} بنجاح. اضغط بالأسفل إذا لم يظهر مثبت التحديث تلقائياً.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                Button(
                                    onClick = { onInstallReady(state.apkFile, state.info) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StatusAvailableGreen,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 50.dp)
                                ) {
                                    Icon(Icons.Rounded.InstallMobile, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "تثبيت التحديث الآن",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                TextButton(onClick = onPostpone) {
                                    Text("التحديث لاحقاً")
                                }
                            }

                            is AppUpdateState.Error -> {
                                Icon(
                                    imageVector = Icons.Rounded.ErrorOutline,
                                    contentDescription = null,
                                    tint = StatusBillRed,
                                    modifier = Modifier.size(38.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "تعذر إكمال التحديث",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val info = state.info
                                            if (info != null) {
                                                onStartDownload(info)
                                            } else {
                                                onRetryCheck()
                                            }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AmberGold,
                                            contentColor = Color(0xFF1A1104)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    ) {
                                        Text("إعادة المحاولة")
                                    }
                                    OutlinedButton(
                                        onClick = onPostpone,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    ) {
                                        Text("لاحقاً")
                                    }
                                }
                            }

                            AppUpdateState.Idle -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateDetailsHeader(info: AppUpdateInfo) {
    Text(
        text = "يتوفر تحديث جديد للتطبيق",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    // Version comparison pill
    Surface(
        shape = CircleShape,
        color = AmberGold.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = AmberGold,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "الإصدار ${BuildConfig.VERSION_NAME}  ←  الإصدار ${info.versionName}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = AmberGold
            )
        }
    }

    Spacer(Modifier.height(10.dp))

    DataSafetyBadge()

    Spacer(Modifier.height(12.dp))

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "ما الجديد في هذا التحديث:",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Text(
                text = info.releaseNotes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DataSafetyBadge() {
    Surface(
        shape = CircleShape,
        color = StatusAvailableGreen.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, StatusAvailableGreen.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.VerifiedUser,
                contentDescription = null,
                tint = StatusAvailableGreen,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "آمن تماماً • لن يتم حذف أي طاولات أو فواتير",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = StatusAvailableGreen
            )
        }
    }
}

private fun formatTransferBytes(downloaded: Long, total: Long): String {
    val downMb = downloaded / (1024f * 1024f)
    return if (total > 0L) {
        val totalMb = total / (1024f * 1024f)
        String.format(Locale.US, "%.1f / %.1f MB", downMb, totalMb)
    } else {
        String.format(Locale.US, "%.1f MB", downMb)
    }
}
