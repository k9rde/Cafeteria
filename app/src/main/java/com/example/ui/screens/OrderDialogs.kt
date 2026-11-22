package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.CafeTableEntity
import com.example.data.local.OrderItemEntity
import com.example.data.model.CurrencyOption
import com.example.data.model.TableStatus
import com.example.data.model.formatPrice
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.viewmodel.TableCardUiModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrderNoteDialog(
    item: OrderItemEntity,
    onDismiss: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    val presets = listOf(
        "بدون سكر",
        "سكر خفيف",
        "تبديل رأس",
        "ثلج إضافي",
        "بدون نعناع",
        "سفري",
        "ثقيل"
    )
    var noteText by remember(item.id) { mutableStateOf(item.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ملاحظة لـ ${item.itemName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        val selected = noteText == preset
                        FilterChip(
                            selected = selected,
                            onClick = { noteText = if (selected) "" else preset },
                            label = { Text(preset) }
                        )
                    }
                }
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("ملاحظة مخصصة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSaveNote(noteText)
                onDismiss()
            }) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun ChargesDialog(
    table: CafeTableEntity,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onApply: (discountPercent: Double, serviceCharge: Double) -> Unit
) {
    var discountStr by remember(table.id) {
        mutableStateOf(if (table.discountPercent > 0) table.discountPercent.toInt().toString() else "")
    }
    var serviceStr by remember(table.id) {
        mutableStateOf(if (table.serviceCharge > 0) table.serviceCharge.toInt().toString() else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("الخصم ورسوم الخدمة") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = discountStr,
                    onValueChange = { discountStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("نسبة الخصم (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = serviceStr,
                    onValueChange = { serviceStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("رسوم الخدمة (${currency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val discount = discountStr.toDoubleOrNull() ?: 0.0
                val service = serviceStr.toDoubleOrNull() ?: 0.0
                onApply(discount, service)
                onDismiss()
            }) {
                Text("تطبيق")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun TransferTableDialog(
    currentTable: CafeTableEntity,
    allTables: List<TableCardUiModel>,
    onDismiss: () -> Unit,
    onSelectTarget: (Int) -> Unit
) {
    val availableTargets = allTables.filter {
        it.table.id != currentTable.id && it.table.statusEnum == TableStatus.AVAILABLE
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.SwapHoriz, contentDescription = null, tint = AmberGold)
                Text("نقل ${currentTable.name} إلى:")
            }
        },
        text = {
            if (availableTargets.isEmpty()) {
                Text("لا توجد طاولات فارغة متاحة حالياً للنقل إليها.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableTargets, key = { it.table.id }) { target ->
                        Surface(
                            onClick = {
                                onSelectTarget(target.table.id)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = StatusAvailableGreen.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, StatusAvailableGreen.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = target.table.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "فارغ",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = StatusAvailableGreen
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun ReceiptPreviewDialog(
    receiptText: String,
    tableName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("فاتورة $tableName") },
        text = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = receiptText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(14.dp)
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "فاتورة $tableName - كافتيريا")
                        putExtra(Intent.EXTRA_TEXT, receiptText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة / طباعة الفاتورة"))
                }
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("مشاركة / طباعة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun CheckoutConfirmDialog(
    table: CafeTableEntity,
    grandTotal: Double,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onConfirmCheckout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = StatusAvailableGreen
            )
        },
        title = { Text("تصفية الحساب وإغلاق ${table.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("سيتم تسجيل الفاتورة في الجرد اليومي وإعادة الطاولة إلى حالة (فارغ).")
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("المبلغ المستحق:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = formatPrice(grandTotal, currency),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = AmberGold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmCheckout()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusAvailableGreen)
            ) {
                Text("تأكيد وإغلاق الميز")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
