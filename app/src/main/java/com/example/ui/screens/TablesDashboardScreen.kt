package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.TableRestaurant
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.CurrencyOption
import com.example.data.model.TableFilter
import com.example.data.model.TableStatus
import com.example.data.model.formatPrice
import com.example.ui.components.GlassCard
import com.example.ui.components.LiveElapsedTimerText
import com.example.ui.components.SquircleIconBadge
import com.example.ui.components.StatusGlassBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusBillRed
import com.example.ui.theme.StatusOccupiedAmber
import com.example.ui.viewmodel.CafeteriaUiState
import com.example.ui.viewmodel.TableCardUiModel
import kotlinx.coroutines.launch

@Composable
fun TablesDashboardScreen(
    uiState: CafeteriaUiState,
    onTableClick: (Int) -> Unit,
    onAddTable: (String?) -> Unit,
    onRenameTable: (Int, String) -> Unit,
    onDeleteTable: (Int) -> Unit,
    onToggleEditMode: () -> Unit,
    onFilterChange: (TableFilter) -> Unit,
    onOpenSettings: () -> Unit,
    hasPendingUpdate: Boolean = false,
    onOpenUpdate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var tableToRename by remember { mutableStateOf<TableCardUiModel?>(null) }
    var tableToDelete by remember { mutableStateOf<TableCardUiModel?>(null) }
    var showAddTableDialog by remember { mutableStateOf(false) }
    var showFullLogoPreview by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val canScrollDown by remember {
        derivedStateOf { gridState.canScrollForward }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Adaptive(minSize = 158.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("tables_grid"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 72.dp)
        ) {
            // 1. Scrollable Top Glass Header (scrolls away cleanly when swiping up)
            item(key = "dashboard_header", span = { GridItemSpan(maxLineSpan) }) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_qissa_logo),
                                contentDescription = "شعار قصة كافيه",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, AmberGold.copy(alpha = 0.65f), CircleShape)
                                    .clickable { showFullLogoPreview = true }
                                    .testTag("header_logo_button")
                            )
                            Column {
                                Text(
                                    text = "كافتيريا",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = "إجمالي اليوم: ${
                                        formatPrice(
                                            uiState.dailySummary.totalRevenue,
                                            uiState.settings.currency
                                        )
                                    }  •  ${uiState.dailySummary.closedTablesCount} فاتورة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Add Table (+)
                            Surface(
                                onClick = { showAddTableDialog = true },
                                shape = CircleShape,
                                color = AmberGold.copy(alpha = 0.20f),
                                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("add_table_button")
                            ) {
                                Box(
                                    modifier = Modifier.size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "إضافة ميز",
                                        tint = AmberGold
                                    )
                                }
                            }

                            // Edit / Delete Tables Mode Toggle
                            Surface(
                                onClick = onToggleEditMode,
                                shape = CircleShape,
                                color = if (uiState.isEditTablesMode) {
                                    StatusBillRed.copy(alpha = 0.22f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (uiState.isEditTablesMode) {
                                        StatusBillRed.copy(alpha = 0.6f)
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("edit_tables_button")
                            ) {
                                Box(
                                    modifier = Modifier.size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isEditTablesMode) Icons.Rounded.Remove else Icons.Rounded.Tune,
                                        contentDescription = "تعديل أو حذف الطاولات",
                                        tint = if (uiState.isEditTablesMode) StatusBillRed else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Settings Gear
                            Surface(
                                onClick = onOpenSettings,
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("open_settings_button")
                            ) {
                                Box(
                                    modifier = Modifier.size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Settings,
                                        contentDescription = "الإعدادات والإدارة",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Optional Pending In-App Update Banner (if postponed earlier)
            if (hasPendingUpdate) {
                item(key = "pending_update_banner", span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        onClick = onOpenUpdate,
                        shape = RoundedCornerShape(18.dp),
                        color = AmberGold.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("pending_update_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SystemUpdate,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "يتوفر تحديث جديد جاهز للتثبيت دون حذف البيانات",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "تحديث",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = AmberGold
                            )
                        }
                    }
                }
            }

            // 2. Scrollable Filter Chips Row
            item(key = "dashboard_filters", span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(TableFilter.entries) { filter ->
                        val count = when (filter) {
                            TableFilter.ALL -> uiState.tables.size
                            TableFilter.AVAILABLE -> uiState.availableCount
                            TableFilter.OCCUPIED -> uiState.occupiedCount
                            TableFilter.BILL_REQUESTED -> uiState.billRequestedCount
                        }
                        val isSelected = uiState.tableFilter == filter
                        val chipColor = when (filter) {
                            TableFilter.ALL -> AmberGold
                            TableFilter.AVAILABLE -> StatusAvailableGreen
                            TableFilter.OCCUPIED -> StatusOccupiedAmber
                            TableFilter.BILL_REQUESTED -> StatusBillRed
                        }

                        Surface(
                            onClick = { onFilterChange(filter) },
                            shape = CircleShape,
                            color = if (isSelected) {
                                chipColor.copy(alpha = 0.24f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = uiState.settings.glassOpacity.coerceAtLeast(0.28f))
                            },
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) chipColor else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("filter_${filter.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = filter.labelAr,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = chipColor.copy(alpha = 0.22f)
                                ) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = chipColor,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Empty State or Table Cards
            if (uiState.filteredTables.isEmpty()) {
                item(key = "empty_tables", span = { GridItemSpan(maxLineSpan) }) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.TableRestaurant,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "لا توجد طاولات في هذا التصنيف",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Button(
                                onClick = { showAddTableDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("إضافة ميز جديد")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = uiState.filteredTables,
                    key = { it.table.id },
                    contentType = { "table_card" }
                ) { model ->
                    TableGridCard(
                        model = model,
                        currency = uiState.settings.currency,
                        isSelected = uiState.selectedTableId == model.table.id,
                        isEditMode = uiState.isEditTablesMode,
                        onClick = { onTableClick(model.table.id) },
                        onRenameClick = { tableToRename = model },
                        onDeleteClick = { tableToDelete = model },
                        modifier = Modifier.animateItem()
                    )
                }

                // Quick Add Table card at the end of the grid
                item(key = "quick_add_table_card") {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(136.dp)
                            .testTag("grid_add_table_card"),
                        shape = RoundedCornerShape(24.dp),
                        borderColor = AmberGold.copy(alpha = 0.45f),
                        onClick = { showAddTableDialog = true }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            SquircleIconBadge(
                                iconKey = "sparkles",
                                accentColor = AmberGold,
                                size = 40.dp,
                                iconSize = 20.dp
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "+ إضافة ميز جديد",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = AmberGold
                            )
                        }
                    }
                }
            }
        }

        // Floating pill prompt when more tables exist below (tap or swipe up)
        AnimatedVisibility(
            visible = canScrollDown,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            Surface(
                onClick = {
                    coroutineScope.launch {
                        val lastIdx = (gridState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)
                        gridState.animateScrollToItem(lastIdx)
                    }
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.65f)),
                shadowElevation = 6.dp,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "اسحب للأعلى لعرض باقي الميوزة",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // Full-size Circular Transparent Logo Preview
    if (showFullLogoPreview) {
        Dialog(
            onDismissRequest = { showFullLogoPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showFullLogoPreview = false }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_qissa_logo),
                    contentDescription = "شعار قصة كافيه بالحجم الكامل",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(290.dp)
                        .shadow(24.dp, CircleShape, clip = false)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { showFullLogoPreview = false }
                        )
                        .testTag("full_logo_preview_image")
                )
            }
        }
    }

    // Add Table Dialog
    if (showAddTableDialog) {
        var nameInput by remember { mutableStateOf("ميز ${uiState.tables.size + 1}") }
        AlertDialog(
            onDismissRequest = { showAddTableDialog = false },
            title = { Text("إضافة طاولة جديدة") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("اسم أو رقم الميز") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddTable(nameInput)
                        showAddTableDialog = false
                    },
                    modifier = Modifier.testTag("confirm_add_table_button")
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTableDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Rename Table Dialog
    tableToRename?.let { target ->
        var updatedName by remember(target.table.id) { mutableStateOf(target.table.name) }
        AlertDialog(
            onDismissRequest = { tableToRename = null },
            title = { Text("تعديل اسم الميز") },
            text = {
                OutlinedTextField(
                    value = updatedName,
                    onValueChange = { updatedName = it },
                    label = { Text("الاسم الجديد") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameTable(target.table.id, updatedName)
                        tableToRename = null
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { tableToRename = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Delete Table Confirmation Dialog
    tableToDelete?.let { target ->
        val isBusy = target.table.statusEnum != TableStatus.AVAILABLE
        AlertDialog(
            onDismissRequest = { tableToDelete = null },
            title = { Text("حذف ${target.table.name}؟") },
            text = {
                Text(
                    if (isBusy) {
                        "تنبيه: هذا الميز مشغول حالياً ويحتوي على ${target.itemCount} أصناف بقيمة ${
                            formatPrice(target.grandTotal, uiState.settings.currency)
                        }. هل أنت متأكد من حذفه؟"
                    } else {
                        "سيتم حذف ${target.table.name} نهائياً من لوحة الطاولات."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTable(target.table.id)
                        tableToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusBillRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { tableToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun TableGridCard(
    model: TableCardUiModel,
    currency: CurrencyOption,
    isSelected: Boolean,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = model.table.statusEnum
    val accentColor = when (status) {
        TableStatus.AVAILABLE -> StatusAvailableGreen
        TableStatus.OCCUPIED -> StatusOccupiedAmber
        TableStatus.BILL_REQUESTED -> StatusBillRed
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("table_card_${model.table.id}"),
        shape = RoundedCornerShape(24.dp),
        accentTint = if (status != TableStatus.AVAILABLE || isSelected) accentColor else null,
        borderColor = if (isSelected) AmberGold else if (status != TableStatus.AVAILABLE) accentColor else null,
        onClick = onClick
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
                SquircleIconBadge(
                    iconKey = if (status == TableStatus.AVAILABLE) "coffee" else "hookah",
                    accentColor = accentColor,
                    size = 40.dp,
                    iconSize = 20.dp
                )

                StatusGlassBadge(status = status)
            }

            Text(
                text = model.table.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (status == TableStatus.AVAILABLE) {
                Text(
                    text = "جاهز لاستقبال الزبائن",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                        LiveElapsedTimerText(
                            sinceMillis = model.table.occupiedSinceMillis,
                            color = accentColor
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${model.itemCount} أصناف",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = formatPrice(model.grandTotal, currency),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isEditMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = onRenameClick,
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "تعديل الاسم",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "تسمية",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "حذف الطاولة",
                            tint = StatusBillRed
                        )
                    }
                }
            }
        }
    }
}
