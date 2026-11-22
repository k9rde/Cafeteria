package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.CafeTableEntity
import com.example.data.local.OrderItemEntity
import com.example.data.model.CurrencyOption
import com.example.data.model.TableStatus
import com.example.data.model.formatDiscountPercent
import com.example.data.model.formatPrice
import com.example.ui.components.GlassCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StatusAvailableGreen
import com.example.ui.viewmodel.TableCardUiModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        mutableStateOf(if (table.discountPercent > 0) formatDiscountPercent(table.discountPercent) else "")
    }
    var serviceStr by remember(table.id) {
        mutableStateOf(
            if (table.serviceCharge > 0) {
                if (table.serviceCharge % 1.0 == 0.0) table.serviceCharge.toInt().toString()
                else table.serviceCharge.toString()
            } else ""
        )
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
    table: CafeTableEntity,
    items: List<OrderItemEntity>,
    currency: CurrencyOption,
    receiptText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateLabel = remember {
        SimpleDateFormat("yyyy/MM/dd  •  hh:mm a", Locale("ar")).format(Date())
    }
    val subtotal = items.sumOf { it.lineTotal }
    val discountAmount = subtotal * (table.discountPercent / 100.0)
    val grandTotal = (subtotal - discountAmount + table.serviceCharge).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            opacityMultiplier = 1.25f,
            borderColor = AmberGold.copy(alpha = 0.45f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo & Cafe Title
                Image(
                    painter = painterResource(id = R.drawable.img_qissa_logo),
                    contentDescription = "شعار قصة كافيه",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, AmberGold.copy(alpha = 0.75f), CircleShape)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "قصة كافيه",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "فاتورة ضيافة",
                    style = MaterialTheme.typography.labelMedium,
                    color = AmberGold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                // Table & Date Metadata Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الطاولة: ${table.name}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الصنف",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "الكمية",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(48.dp)
                    )
                    Text(
                        text = "السعر",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(88.dp)
                    )
                }

                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))

                // Scrollable Itemized Rows
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.itemName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (item.note.isNotBlank()) {
                                    Text(
                                        text = "ملاحظة: ${item.note}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Text(
                                text = "×${item.quantity}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(48.dp)
                            )
                            Text(
                                text = formatPrice(item.lineTotal, currency),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(88.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                Spacer(Modifier.height(10.dp))

                // Subtotal, Discount, Service
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

                if (discountAmount > 0.0) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "الخصم (${formatDiscountPercent(table.discountPercent)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberGold
                        )
                        Text(
                            text = "-${formatPrice(discountAmount, currency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberGold
                        )
                    }
                }

                if (table.serviceCharge > 0.0) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "رسوم الخدمة",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberGold
                        )
                        Text(
                            text = "+${formatPrice(table.serviceCharge, currency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberGold
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Grand Total Highlight Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AmberGold.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المجموع الكلي",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatPrice(grandTotal, currency),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = AmberGold
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Warm Hospitality Footer
                Row(
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
                        text = "تشرفنا بزيارتكم",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = AmberGold
                    )
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "نتمنى لكم أوقاتاً سعيدة وقهوة تليق بذوقكم • نسعد بخدمتكم دائماً",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "فاتورة ${table.name} - قصة كافيه")
                                putExtra(Intent.EXTRA_TEXT, receiptText)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة / طباعة الفاتورة"))
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
                        Icon(Icons.Rounded.Share, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("مشاركة / طباعة")
                    }

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text("إغلاق")
                    }
                }
            }
        }
    }
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
