package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TableRestaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.CafeteriaDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.repository.CafeteriaRepository
import com.example.data.updater.AppUpdateManager
import com.example.ui.components.CafeteriaWallpaperBackground
import com.example.ui.components.GlassCard
import com.example.ui.screens.AppUpdateDialog
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TableOrderPanel
import com.example.ui.screens.TablesDashboardScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CafeteriaTheme
import com.example.ui.viewmodel.CafeteriaViewModel
import com.example.ui.viewmodel.CafeteriaViewModelFactory
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var updateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = CafeteriaDatabase.getInstance(applicationContext)
        val settingsDataStore = SettingsDataStore(applicationContext)
        val repository = CafeteriaRepository(database.cafeDao(), settingsDataStore)
        val factory = CafeteriaViewModelFactory(repository)

        updateManager = AppUpdateManager(applicationContext)
        updateManager.cleanupOldUpdateFiles()

        setContent {
            val viewModel: CafeteriaViewModel = viewModel(factory = factory)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                viewModel.events.collect { message ->
                    snackbarHostState.showSnackbar(message)
                }
            }

            LaunchedEffect(Unit) {
                delay(1200L)
                updateManager.checkForUpdates(manual = false)
            }

            CafeteriaTheme(
                darkTheme = uiState.settings.isDarkMode,
                glassOpacity = uiState.settings.glassOpacity,
                blurIntensity = uiState.settings.blurIntensity
            ) {
                CafeteriaAppRoot(
                    viewModel = viewModel,
                    updateManager = updateManager,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::updateManager.isInitialized) {
            updateManager.onHostResumed()
        }
    }
}

@Composable
private fun CafeteriaAppRoot(
    viewModel: CafeteriaViewModel,
    updateManager: AppUpdateManager,
    snackbarHostState: SnackbarHostState
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val updateState by updateManager.state.collectAsStateWithLifecycle()
    val postponedUpdate by updateManager.postponedUpdate.collectAsStateWithLifecycle()
    val isUpdateDialogVisible by updateManager.isDialogVisible.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        CafeteriaWallpaperBackground(
            wallpaper = uiState.settings.wallpaper,
            isDarkMode = uiState.settings.isDarkMode,
            blurIntensity = uiState.settings.blurIntensity
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val isTabletOrWide = maxWidth >= 780.dp

                AnimatedContent(
                    targetState = showSettings,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                    label = "root_screen_transition"
                ) { inSettings ->
                    if (inSettings) {
                        SettingsScreen(
                            uiState = uiState,
                            viewModel = viewModel,
                            onClose = { showSettings = false },
                            hasPendingUpdate = postponedUpdate != null,
                            onCheckForUpdates = { updateManager.reopenPostponedUpdate() }
                        )
                    } else if (isTabletOrWide) {
                        // Tablet / iPadOS Split-Pane Layout
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(end = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TablesDashboardScreen(
                                uiState = uiState,
                                onTableClick = { viewModel.selectTable(it) },
                                onAddTable = { viewModel.addTable(it) },
                                onRenameTable = { id, name -> viewModel.renameTable(id, name) },
                                onDeleteTable = { viewModel.deleteTable(it) },
                                onToggleEditMode = { viewModel.toggleEditTablesMode() },
                                onFilterChange = { viewModel.setTableFilter(it) },
                                onOpenSettings = { showSettings = true },
                                hasPendingUpdate = postponedUpdate != null,
                                onOpenUpdate = { updateManager.reopenPostponedUpdate() },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            )

                            val selected = uiState.selectedTable
                            Box(
                                modifier = Modifier
                                    .width(420.dp)
                                    .fillMaxHeight()
                                    .padding(vertical = 10.dp)
                            ) {
                                if (selected != null) {
                                    TableOrderPanel(
                                        uiState = uiState,
                                        tableModel = selected,
                                        showBackButton = false,
                                        onClosePanel = { viewModel.selectTable(null) },
                                        onSelectCategory = { viewModel.selectCategory(it) },
                                        onAddMenuItem = { viewModel.addMenuItemToSelectedTable(it) },
                                        onIncrementOrderItem = { viewModel.incrementOrderItem(it) },
                                        onDecrementOrderItem = { viewModel.decrementOrderItem(it) },
                                        onUpdateOrderNote = { id, note -> viewModel.updateOrderItemNote(id, note) },
                                        onToggleBillRequested = { viewModel.toggleBillRequested(selected.table.id) },
                                        onUpdateCharges = { disc, svc -> viewModel.updateTableCharges(selected.table.id, disc, svc) },
                                        onTransferTable = { targetId -> viewModel.transferTable(selected.table.id, targetId) },
                                        onCheckoutTable = { viewModel.checkoutAndClearTable(selected.table.id) },
                                        onBuildReceipt = {
                                            viewModel.buildReceiptText(
                                                selected.table,
                                                uiState.selectedTableOrderItems,
                                                uiState.settings.currency
                                            )
                                        }
                                    )
                                } else {
                                    EmptyOrderSidePane()
                                }
                            }
                        }
                    } else {
                        // Smartphone Adaptive Flow with smooth animated transitions
                        val selected = uiState.selectedTable
                        AnimatedContent(
                            targetState = selected?.table?.id,
                            transitionSpec = {
                                if (targetState != null) {
                                    (fadeIn(tween(220)) + slideInHorizontally(tween(240)) { -it / 4 })
                                        .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(200)) { it / 6 })
                                } else {
                                    (fadeIn(tween(220)) + slideInHorizontally(tween(240)) { it / 6 })
                                        .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(200)) { -it / 4 })
                                }
                            },
                            label = "phone_table_transition"
                        ) { targetTableId ->
                            val currentSelected = targetTableId?.let { id ->
                                uiState.tables.firstOrNull { it.table.id == id }
                            }
                            if (currentSelected != null) {
                                BackHandler {
                                    viewModel.selectTable(null)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    TableOrderPanel(
                                        uiState = uiState,
                                        tableModel = currentSelected,
                                        showBackButton = true,
                                        onClosePanel = { viewModel.selectTable(null) },
                                        onSelectCategory = { viewModel.selectCategory(it) },
                                        onAddMenuItem = { viewModel.addMenuItemToSelectedTable(it) },
                                        onIncrementOrderItem = { viewModel.incrementOrderItem(it) },
                                        onDecrementOrderItem = { viewModel.decrementOrderItem(it) },
                                        onUpdateOrderNote = { id, note -> viewModel.updateOrderItemNote(id, note) },
                                        onToggleBillRequested = { viewModel.toggleBillRequested(currentSelected.table.id) },
                                        onUpdateCharges = { disc, svc -> viewModel.updateTableCharges(currentSelected.table.id, disc, svc) },
                                        onTransferTable = { targetId -> viewModel.transferTable(currentSelected.table.id, targetId) },
                                        onCheckoutTable = { viewModel.checkoutAndClearTable(currentSelected.table.id) },
                                        onBuildReceipt = {
                                            viewModel.buildReceiptText(
                                                currentSelected.table,
                                                uiState.selectedTableOrderItems,
                                                uiState.settings.currency
                                            )
                                        }
                                    )
                                }
                            } else {
                                TablesDashboardScreen(
                                    uiState = uiState,
                                    onTableClick = { viewModel.selectTable(it) },
                                    onAddTable = { viewModel.addTable(it) },
                                    onRenameTable = { id, name -> viewModel.renameTable(id, name) },
                                    onDeleteTable = { viewModel.deleteTable(it) },
                                    onToggleEditMode = { viewModel.toggleEditTablesMode() },
                                    onFilterChange = { viewModel.setTableFilter(it) },
                                    onOpenSettings = { showSettings = true },
                                    hasPendingUpdate = postponedUpdate != null,
                                    onOpenUpdate = { updateManager.reopenPostponedUpdate() }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isUpdateDialogVisible) {
            AppUpdateDialog(
                updateState = updateState,
                onStartDownload = { info -> updateManager.startUpdateDownload(info) },
                onPostpone = { updateManager.postponeUpdate() },
                onCancelDownload = { updateManager.cancelDownload() },
                onInstallReady = { file, info -> updateManager.installDownloadedApk(file, info) },
                onOpenPermissionSettings = { updateManager.openInstallPermissionSettings() },
                onRetryCheck = { updateManager.checkForUpdates(manual = true) }
            )
        }
    }
}

@Composable
private fun EmptyOrderSidePane() {
    GlassCard(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.TableRestaurant,
                contentDescription = null,
                tint = AmberGold,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "اختر طاولة لبدء الطلب",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "اضغط على أي ميز من الشبكة لعرض الفاتورة وإضافة الأراكيل والمشروبات والحلويات",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
