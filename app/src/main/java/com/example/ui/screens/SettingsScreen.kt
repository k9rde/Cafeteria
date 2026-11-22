package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.MenuCategoryEntity
import com.example.data.local.MenuItemEntity
import com.example.data.model.CurrencyOption
import com.example.data.model.WallpaperOption
import com.example.data.model.formatPrice
import com.example.ui.components.DeveloperContactCard
import com.example.ui.components.GlassCard
import com.example.ui.components.SquircleIconBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusBillRed
import com.example.ui.viewmodel.CafeteriaUiState
import com.example.ui.viewmodel.CafeteriaViewModel

@Composable
fun SettingsScreen(
    uiState: CafeteriaUiState,
    viewModel: CafeteriaViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val pinRequired = uiState.settings.ownerPin.isNotBlank()
    var isUnlocked by remember(pinRequired) { mutableStateOf(!pinRequired) }

    if (!isUnlocked && pinRequired) {
        PinUnlockOverlay(
            expectedPin = uiState.settings.ownerPin,
            onUnlocked = { isUnlocked = true },
            onCancel = onClose
        )
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("المظهر والشفافية", "الجرد اليومي", "إدارة المنيو", "الطاولات")

    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<MenuCategoryEntity?>(null) }

    var showMenuItemDialog by remember { mutableStateOf(false) }
    var editingMenuItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    var defaultCategoryForNewItem by remember { mutableIntStateOf(uiState.categories.firstOrNull()?.id ?: 0) }

    var showPinDialog by remember { mutableStateOf(false) }
    var showResetShiftDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 920.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = onClose,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("settings_back_button")
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "رجوع"
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "الإعدادات وإدارة المالك",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "الشفافية • المنيو • العملة • الجرد اليومي",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        onClick = { showPinDialog = true },
                        shape = CircleShape,
                        color = AmberGold.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f)),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (uiState.settings.ownerPin.isBlank()) "تعيين PIN" else "PIN مفعل",
                                style = MaterialTheme.typography.labelMedium,
                                color = AmberGold
                            )
                        }
                    }
                }
            }

            // Tab Strip
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs.indices.toList()) { idx ->
                    val selected = selectedTab == idx
                    Surface(
                        onClick = { selectedTab = idx },
                        shape = CircleShape,
                        color = if (selected) {
                            AmberGold.copy(alpha = 0.26f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                        },
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) AmberGold else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("settings_tab_$idx")
                    ) {
                        Text(
                            text = tabs[idx],
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) AmberGold else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                when (selectedTab) {
                    0 -> item {
                        AppearanceAndGeneralSection(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }

                    1 -> item {
                        DailySalesSummarySection(
                            uiState = uiState,
                            onResetShiftClick = { showResetShiftDialog = true }
                        )
                    }

                    2 -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الأقسام والأصناف",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            editingCategory = null
                                            showCategoryDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("قسم")
                                    }
                                    Button(
                                        onClick = {
                                            editingMenuItem = null
                                            defaultCategoryForNewItem = uiState.categories.firstOrNull()?.id ?: 0
                                            showMenuItemDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("صنف")
                                    }
                                }
                            }
                        }

                        items(uiState.categories, key = { it.id }) { category ->
                            val itemsInCategory = uiState.menuItems.filter { it.categoryId == category.id }
                            CategoryManagementCard(
                                category = category,
                                items = itemsInCategory,
                                currency = uiState.settings.currency,
                                onEditCategory = {
                                    editingCategory = category
                                    showCategoryDialog = true
                                },
                                onDeleteCategory = { viewModel.deleteCategory(category.id) },
                                onAddItem = {
                                    editingMenuItem = null
                                    defaultCategoryForNewItem = category.id
                                    showMenuItemDialog = true
                                },
                                onEditItem = { item ->
                                    editingMenuItem = item
                                    defaultCategoryForNewItem = category.id
                                    showMenuItemDialog = true
                                },
                                onDeleteItem = { item -> viewModel.deleteMenuItem(item.id) }
                            )
                        }
                    }

                    3 -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("إدارة الطاولات (${uiState.tables.size})", style = MaterialTheme.typography.titleLarge)
                                Button(
                                    onClick = { viewModel.addTable() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("إضافة ميز")
                                }
                            }
                        }

                        items(uiState.tables, key = { it.table.id }) { model ->
                            GlassCard(
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(model.table.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = model.table.statusEnum.labelAr,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteTable(model.table.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "حذف ${model.table.name}",
                                            tint = StatusBillRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCategoryDialog) {
        CategoryEditorDialog(
            existing = editingCategory,
            onDismiss = { showCategoryDialog = false },
            onSave = { id, name, iconKey, colorHex ->
                viewModel.saveCategory(id, name, iconKey, colorHex)
            }
        )
    }

    if (showMenuItemDialog) {
        MenuItemEditorDialog(
            existing = editingMenuItem,
            categories = uiState.categories,
            initialCategoryId = defaultCategoryForNewItem,
            currency = uiState.settings.currency,
            onDismiss = { showMenuItemDialog = false },
            onSave = { id, catId, name, price, iconKey ->
                viewModel.saveMenuItem(id, catId, name, price, iconKey)
            }
        )
    }

    if (showPinDialog) {
        PinSetupDialog(
            currentPin = uiState.settings.ownerPin,
            onDismiss = { showPinDialog = false },
            onSavePin = { viewModel.updateOwnerPin(it) }
        )
    }

    if (showResetShiftDialog) {
        AlertDialog(
            onDismissRequest = { showResetShiftDialog = false },
            title = { Text("تصفير الوردية اليومية؟") },
            text = { Text("سيتم تصفير إجمالي إيرادات اليوم وسجل الطاولات المغلقة والأصناف الأكثر طلباً للبدء بوردية جديدة.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDailyShift()
                        showResetShiftDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusBillRed)
                ) {
                    Text("تأكيد التصفير")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetShiftDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearanceAndGeneralSection(
    uiState: CafeteriaUiState,
    viewModel: CafeteriaViewModel
) {
    val settings = uiState.settings

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Glass Transparency & Blur Sliders
        GlassCard(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("التحكم بشفافية الواجهة والضبابية", style = MaterialTheme.typography.titleLarge)

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("شفافية البطاقات الزجاجية", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${(settings.glassOpacity * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = AmberGold
                        )
                    }
                    Slider(
                        value = settings.glassOpacity,
                        onValueChange = { viewModel.updateGlassOpacity(it) },
                        valueRange = 0.14f..0.85f,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberGold,
                            activeTrackColor = AmberGold
                        ),
                        modifier = Modifier.testTag("glass_opacity_slider")
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("شدة الضبابية (Blur)", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${settings.blurIntensity.toInt()} dp",
                            style = MaterialTheme.typography.labelLarge,
                            color = AmberGold
                        )
                    }
                    Slider(
                        value = settings.blurIntensity,
                        onValueChange = { viewModel.updateBlurIntensity(it) },
                        valueRange = 0f..28f,
                        colors = SliderDefaults.colors(
                            thumbColor = AmberGold,
                            activeTrackColor = AmberGold
                        ),
                        modifier = Modifier.testTag("glass_blur_slider")
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                // Dark / Light Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (settings.isDarkMode) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                            contentDescription = null,
                            tint = AmberGold
                        )
                        Column {
                            Text("المظهر الداكن / الفاتح", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = if (settings.isDarkMode) "الوضع الليلي الزجاجي مفعل" else "الوضع النهاري الزجاجي مفعل",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = settings.isDarkMode,
                        onCheckedChange = { viewModel.updateDarkMode(it) },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // Wallpapers
        GlassCard(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("خلفيات الواجهة", style = MaterialTheme.typography.titleLarge)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(WallpaperOption.entries) { wp ->
                        val selected = settings.wallpaper == wp
                        val resId = when (wp) {
                            WallpaperOption.ESPRESSO -> R.drawable.img_bg_espresso_lounge
                            WallpaperOption.OCEAN -> R.drawable.img_bg_ocean_glass
                            WallpaperOption.EMERALD -> R.drawable.img_bg_emerald_mist
                            WallpaperOption.CLASSIC -> null
                        }
                        Surface(
                            onClick = { viewModel.updateWallpaper(wp) },
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) AmberGold else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .size(width = 112.dp, height = 144.dp)
                                .testTag("wallpaper_${wp.key}")
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (resId != null) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = wp.labelAr,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF1A1722), Color(0xFF2B2435))
                                                )
                                            )
                                    )
                                }
                                Surface(
                                    color = Color.Black.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = wp.labelAr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Currency Selector
        GlassCard(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("العملة المحلية", style = MaterialTheme.typography.titleLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurrencyOption.entries.forEach { option ->
                        FilterChip(
                            selected = settings.currency == option,
                            onClick = { viewModel.updateCurrency(option) },
                            label = {
                                Text(
                                    text = "${option.symbol} (${option.labelAr})",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = AmberGold.copy(alpha = 0.24f),
                                selectedLabelColor = AmberGold
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = settings.currency == option,
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = AmberGold
                            )
                        )
                    }
                }
            }
        }

        DeveloperContactCard()
    }
}

@Composable
private fun DailySalesSummarySection(
    uiState: CafeteriaUiState,
    onResetShiftClick: () -> Unit
) {
    val summary = uiState.dailySummary
    val currency = uiState.settings.currency

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GlassCard(
            shape = RoundedCornerShape(24.dp),
            accentTint = AmberGold,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SquircleIconBadge(
                            iconKey = "star",
                            accentColor = AmberGold,
                            size = 44.dp,
                            iconSize = 22.dp
                        )
                        Column {
                            Text("الجرد اليومي", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = "ملخص مبيعات الوردية الحالية",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onResetShiftClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusBillRed.copy(alpha = 0.20f),
                            contentColor = StatusBillRed
                        ),
                        border = BorderStroke(1.dp, StatusBillRed.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("reset_shift_button")
                    ) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("تصفير الوردية")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("إجمالي الإيرادات", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = formatPrice(summary.totalRevenue, currency),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusAvailableGreen
                            )
                        }
                    }

                    GlassCard(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("الطاولات المغلقة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${summary.closedTablesCount} طاولة",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = AmberGold
                            )
                        }
                    }
                }

                // Highlights: Top Hookah & Top Drink
                if (summary.topHookah != null || summary.topDrink != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        summary.topHookah?.let { hookah ->
                            GlassCard(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("الأركيلة الأكثر طلباً", style = MaterialTheme.typography.labelSmall, color = AmberGold)
                                    Text(hookah.itemName, style = MaterialTheme.typography.titleSmall)
                                    Text("${hookah.totalQuantity} طلب", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        summary.topDrink?.let { drink ->
                            GlassCard(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("المشروب الأكثر طلباً", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                    Text(drink.itemName, style = MaterialTheme.typography.titleSmall)
                                    Text("${drink.totalQuantity} طلب", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Ordered Items
        GlassCard(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = AmberGold)
                    Text("الأصناف الأكثر مبيعاً اليوم", style = MaterialTheme.typography.titleLarge)
                }

                if (summary.topItems.isEmpty()) {
                    Text(
                        text = "لم يتم إغلاق أي فواتير بعد في هذه الوردية.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val maxQty = summary.topItems.maxOf { it.totalQuantity }.coerceAtLeast(1)
                    summary.topItems.take(8).forEach { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.itemName} (${item.categoryName})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "${item.totalQuantity} ×  •  ${formatPrice(item.totalRevenue, currency)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AmberGold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { item.totalQuantity.toFloat() / maxQty.toFloat() },
                                color = AmberGold,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryManagementCard(
    category: MenuCategoryEntity,
    items: List<MenuItemEntity>,
    currency: CurrencyOption,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (MenuItemEntity) -> Unit,
    onDeleteItem: (MenuItemEntity) -> Unit
) {
    val catColor = Color(category.colorHex)

    GlassCard(
        shape = RoundedCornerShape(22.dp),
        accentTint = catColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SquircleIconBadge(
                        iconKey = category.iconKey,
                        accentColor = catColor,
                        size = 40.dp,
                        iconSize = 20.dp
                    )
                    Column {
                        Text(category.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "${items.size} صنف",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onAddItem) {
                        Icon(Icons.Rounded.Add, contentDescription = "إضافة صنف")
                    }
                    IconButton(onClick = onEditCategory) {
                        Icon(Icons.Rounded.Edit, contentDescription = "تعديل القسم")
                    }
                    IconButton(onClick = onDeleteCategory) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف القسم", tint = StatusBillRed)
                    }
                }
            }

            if (items.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                items.forEach { menuItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            SquircleIconBadge(
                                iconKey = menuItem.iconKey,
                                accentColor = catColor,
                                size = 32.dp,
                                iconSize = 16.dp
                            )
                            Column {
                                Text(menuItem.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text(
                                    text = formatPrice(menuItem.price, currency),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberGold
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { onEditItem(menuItem) }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "تعديل الصنف", modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDeleteItem(menuItem) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "حذف الصنف", tint = StatusBillRed, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
