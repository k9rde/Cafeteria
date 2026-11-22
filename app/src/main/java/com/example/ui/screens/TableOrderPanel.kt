package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.MenuItemEntity
import com.example.data.local.OrderItemEntity
import com.example.data.model.CurrencyOption
import com.example.data.model.TableStatus
import com.example.data.model.formatDiscountPercent
import com.example.data.model.formatPrice
import com.example.ui.components.GlassCard
import com.example.ui.components.LiveElapsedTimerText
import com.example.ui.components.SquircleIconBadge
import com.example.ui.components.StatusGlassBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.theme.StatusBillRed
import com.example.ui.viewmodel.CafeteriaUiState
import com.example.ui.viewmodel.TableCardUiModel

@Composable
fun TableOrderPanel(
    uiState: CafeteriaUiState,
    tableModel: TableCardUiModel,
    showBackButton: Boolean,
    onClosePanel: () -> Unit,
    onSelectCategory: (Int) -> Unit,
    onAddMenuItem: (MenuItemEntity) -> Unit,
    onIncrementOrderItem: (Int) -> Unit,
    onDecrementOrderItem: (Int) -> Unit,
    onUpdateOrderNote: (Int, String) -> Unit,
    onToggleBillRequested: () -> Unit,
    onUpdateCharges: (Double, Double) -> Unit,
    onTransferTable: (Int) -> Unit,
    onCheckoutTable: () -> Unit,
    onBuildReceipt: () -> String,
    modifier: Modifier = Modifier
) {
    val table = tableModel.table
    val currency = uiState.settings.currency
    var cachedOrderItems by remember(table.id) { mutableStateOf(uiState.selectedTableOrderItems) }
    if (uiState.selectedTableId == table.id) {
        cachedOrderItems = uiState.selectedTableOrderItems
    }
    val orderItems = cachedOrderItems
    val filteredMenuItems = remember(uiState.menuItems, uiState.selectedCategoryId) {
        uiState.menuItems.filter { it.categoryId == uiState.selectedCategoryId && it.isAvailable }
    }

    var noteTargetItem by remember(table.id) { mutableStateOf<OrderItemEntity?>(null) }
    var showChargesDialog by remember(table.id) { mutableStateOf(false) }
    var showTransferDialog by remember(table.id) { mutableStateOf(false) }
    var receiptToPreview by remember(table.id) { mutableStateOf<String?>(null) }
    var showCheckoutDialog by remember(table.id) { mutableStateOf(false) }

    val subtotal = orderItems.sumOf { it.lineTotal }
    val discountAmount = subtotal * (table.discountPercent / 100.0)
    val grandTotal = (subtotal - discountAmount + table.serviceCharge).coerceAtLeast(0.0)

    GlassCard(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(28.dp),
        opacityMultiplier = 1.08f
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Order Panel Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (showBackButton) {
                        Surface(
                            onClick = onClosePanel,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("back_to_tables_button")
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "العودة للطاولات"
                                )
                            }
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = table.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusGlassBadge(status = table.statusEnum)
                        }
                        if (table.occupiedSinceMillis != null) {
                            LiveElapsedTimerText(sinceMillis = table.occupiedSinceMillis)
                        }
                    }
                }

                if (table.statusEnum != TableStatus.AVAILABLE) {
                    val isBillReq = table.statusEnum == TableStatus.BILL_REQUESTED
                    Surface(
                        onClick = onToggleBillRequested,
                        shape = CircleShape,
                        color = if (isBillReq) StatusBillRed.copy(alpha = 0.25f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        border = BorderStroke(
                            1.dp,
                            if (isBillReq) StatusBillRed else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_bill_requested_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Receipt,
                                contentDescription = null,
                                tint = if (isBillReq) StatusBillRed else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isBillReq) "إلغاء الطلب" else "طلب الحساب",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isBillReq) StatusBillRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            // 2. Top iOS Glossy Category Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.categories, key = { it.id }) { category ->
                    val selected = category.id == uiState.selectedCategoryId
                    val catColor = Color(category.colorHex)
                    Surface(
                        onClick = { onSelectCategory(category.id) },
                        shape = RoundedCornerShape(18.dp),
                        color = if (selected) {
                            catColor.copy(alpha = 0.24f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                        },
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("category_tab_${category.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SquircleIconBadge(
                                iconKey = category.iconKey,
                                accentColor = catColor,
                                size = 34.dp,
                                iconSize = 18.dp
                            )
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. Menu Selection Grid + Active Order Cart Split Vertically
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Menu Items Grid
                Text(
                    text = "اختر من المنيو للإضافة",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val selectedCategoryColor = Color(
                    uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId }?.colorHex
                        ?: 0xFFFF9F0A
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("menu_items_strip"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(filteredMenuItems, key = { it.id }, contentType = { "menu_item" }) { item ->
                        GlassCard(
                            shape = RoundedCornerShape(16.dp),
                            lightweight = true,
                            onClick = { onAddMenuItem(item) },
                            modifier = Modifier
                                .animateItem()
                                .widthIn(min = 142.dp, max = 200.dp)
                                .testTag("menu_item_${item.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SquircleIconBadge(
                                    iconKey = item.iconKey,
                                    accentColor = selectedCategoryColor,
                                    size = 34.dp,
                                    iconSize = 17.dp
                                )
                                Column {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatPrice(item.price, currency),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = AmberGold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                // Active Cart Items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فاتورة الميز (${orderItems.sumOf { it.quantity }} أصناف)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (orderItems.isNotEmpty()) {
                        Surface(
                            onClick = { showChargesDialog = true },
                            shape = CircleShape,
                            color = AmberGold.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Percent,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "خصم / خدمة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberGold
                                )
                            }
                        }
                    }
                }

                if (orderItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لم تتم إضافة أصناف بعد — اضغط على أي صنف أعلاه",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("order_cart_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(orderItems, key = { it.id }, contentType = { "order_row" }) { orderItem ->
                            OrderCartItemRow(
                                item = orderItem,
                                currency = currency,
                                onIncrement = { onIncrementOrderItem(orderItem.id) },
                                onDecrement = { onDecrementOrderItem(orderItem.id) },
                                onNoteClick = { noteTargetItem = orderItem },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }

            // 4. Fixed Smart Billing Footer
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "المجموع الفرعي",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatPrice(subtotal, currency),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (discountAmount > 0.0 || table.serviceCharge > 0.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = buildString {
                                    if (discountAmount > 0.0) append("خصم ${formatDiscountPercent(table.discountPercent)}% ")
                                    if (table.serviceCharge > 0.0) append("• خدمة")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberGold
                            )
                            Text(
                                text = buildString {
                                    if (discountAmount > 0.0) append("-${formatPrice(discountAmount, currency)} ")
                                    if (table.serviceCharge > 0.0) append("+${formatPrice(table.serviceCharge, currency)}")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberGold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المجموع الكلي",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatPrice(grandTotal, currency),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = AmberGold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Print / Share Receipt
                        Surface(
                            onClick = {
                                if (orderItems.isNotEmpty()) {
                                    receiptToPreview = onBuildReceipt()
                                }
                            },
                            enabled = orderItems.isNotEmpty(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .alpha(if (orderItems.isNotEmpty()) 1f else 0.42f)
                                .minimumInteractiveComponentSize()
                                .testTag("print_receipt_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Print,
                                    contentDescription = "طباعة ومشاركة الفاتورة",
                                    modifier = Modifier.size(18.dp)
                                )
                                Text("فاتورة", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Transfer Table
                        Surface(
                            onClick = {
                                if (orderItems.isNotEmpty()) {
                                    showTransferDialog = true
                                }
                            },
                            enabled = orderItems.isNotEmpty(),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.09f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .alpha(if (orderItems.isNotEmpty()) 1f else 0.42f)
                                .minimumInteractiveComponentSize()
                                .testTag("transfer_table_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapHoriz,
                                    contentDescription = "نقل الميز",
                                    modifier = Modifier.size(18.dp)
                                )
                                Text("نقل", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Checkout & Clear Table
                        Button(
                            onClick = { showCheckoutDialog = true },
                            enabled = orderItems.isNotEmpty() || table.statusEnum != TableStatus.AVAILABLE,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusAvailableGreen,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("checkout_table_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "تصفية وإغلاق",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    noteTargetItem?.let { target ->
        OrderNoteDialog(
            item = target,
            onDismiss = { noteTargetItem = null },
            onSaveNote = { note -> onUpdateOrderNote(target.id, note) }
        )
    }

    if (showChargesDialog) {
        ChargesDialog(
            table = table,
            currency = currency,
            onDismiss = { showChargesDialog = false },
            onApply = onUpdateCharges
        )
    }

    if (showTransferDialog) {
        TransferTableDialog(
            currentTable = table,
            allTables = uiState.tables,
            onDismiss = { showTransferDialog = false },
            onSelectTarget = onTransferTable
        )
    }

    receiptToPreview?.let { receipt ->
        ReceiptPreviewDialog(
            table = table,
            items = orderItems,
            currency = currency,
            receiptText = receipt,
            onDismiss = { receiptToPreview = null }
        )
    }

    if (showCheckoutDialog) {
        CheckoutConfirmDialog(
            table = table,
            grandTotal = grandTotal,
            currency = currency,
            onDismiss = { showCheckoutDialog = false },
            onConfirmCheckout = onCheckoutTable
        )
    }
}

@Composable
private fun OrderCartItemRow(
    item: OrderItemEntity,
    currency: CurrencyOption,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onNoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        shape = RoundedCornerShape(16.dp),
        lightweight = true,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatPrice(item.lineTotal, currency),
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberGold
                )
                if (item.note.isNotBlank()) {
                    Text(
                        text = "ملاحظة: ${item.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Surface(
                onClick = onNoteClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = "إضافة ملاحظة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Stepper (-) qty (+)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = onDecrement,
                    shape = CircleShape,
                    color = StatusBillRed.copy(alpha = 0.16f),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "إنقاص الكمية",
                            tint = StatusBillRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = item.quantity.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                Surface(
                    onClick = onIncrement,
                    shape = CircleShape,
                    color = StatusAvailableGreen.copy(alpha = 0.18f),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "زيادة الكمية",
                            tint = StatusAvailableGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
