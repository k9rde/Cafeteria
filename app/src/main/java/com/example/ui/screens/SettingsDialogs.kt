package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.MenuCategoryEntity
import com.example.data.local.MenuItemEntity
import com.example.data.model.CurrencyOption
import com.example.ui.components.CafeIconCatalog
import com.example.ui.components.GlassCard
import com.example.ui.components.SquircleIconBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.StatusBillRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorDialog(
    existing: MenuCategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (id: Int?, name: String, iconKey: String, colorHex: Long) -> Unit
) {
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var iconKey by remember(existing) { mutableStateOf(existing?.iconKey ?: "hookah") }
    var colorHex by remember(existing) { mutableLongStateOf(existing?.colorHex ?: 0xFFFF9F0A) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "إضافة قسم جديد" else "تعديل القسم") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم القسم (مثال: العصائر)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("اختر اللون:", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CafeIconCatalog.colorPresets.forEach { preset ->
                        val selected = colorHex == preset
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(preset))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable { colorHex = preset }
                        )
                    }
                }

                Text("اختر الأيقونة:", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CafeIconCatalog.options.forEach { opt ->
                        val selected = iconKey == opt.key
                        Surface(
                            onClick = { iconKey = opt.key },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) Color(colorHex).copy(alpha = 0.25f) else Color.Transparent,
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) Color(colorHex) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        ) {
                            Box(modifier = Modifier.padding(6.dp)) {
                                SquircleIconBadge(
                                    iconKey = opt.key,
                                    accentColor = Color(colorHex),
                                    size = 36.dp,
                                    iconSize = 18.dp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(existing?.id, name, iconKey, colorHex)
                        onDismiss()
                    }
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MenuItemEditorDialog(
    existing: MenuItemEntity?,
    categories: List<MenuCategoryEntity>,
    initialCategoryId: Int,
    currency: CurrencyOption,
    onDismiss: () -> Unit,
    onSave: (id: Int?, categoryId: Int, name: String, price: Double, iconKey: String) -> Unit
) {
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var priceStr by remember(existing) {
        mutableStateOf(
            existing?.price?.let {
                if (currency.usesDecimals) it.toString() else it.toInt().toString()
            } ?: ""
        )
    }
    var selectedCategoryId by remember(existing, initialCategoryId) {
        mutableStateOf(existing?.categoryId ?: initialCategoryId)
    }
    var iconKey by remember(existing) {
        mutableStateOf(
            existing?.iconKey ?: categories.firstOrNull { it.id == selectedCategoryId }?.iconKey ?: "coffee"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "إضافة صنف جديد" else "تعديل الصنف") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("القسم:", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) }
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الصنف (مثال: تفاحتين / موهيتو)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("السعر (${currency.symbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("الأيقونة:", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CafeIconCatalog.options.forEach { opt ->
                        val selected = iconKey == opt.key
                        val catColor = Color(
                            categories.firstOrNull { it.id == selectedCategoryId }?.colorHex ?: opt.defaultColorHex
                        )
                        Surface(
                            onClick = { iconKey = opt.key },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) catColor.copy(alpha = 0.24f) else Color.Transparent,
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        ) {
                            Box(modifier = Modifier.padding(6.dp)) {
                                SquircleIconBadge(
                                    iconKey = opt.key,
                                    accentColor = catColor,
                                    size = 34.dp,
                                    iconSize = 18.dp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull()
                    if (name.isNotBlank() && price != null && price >= 0.0) {
                        onSave(existing?.id, selectedCategoryId, name, price, iconKey)
                        onDismiss()
                    }
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun PinSetupDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onSavePin: (String) -> Unit
) {
    var pinInput by remember(currentPin) { mutableStateOf(currentPin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("رمز حماية المالك (PIN)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "أدخل رمزاً من 4 أرقام لحماية الإعدادات وإدارة المنيو والجرد اليومي. اتركه فارغاً لإلغاء القفل.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { value ->
                        pinInput = value.filter { it.isDigit() }.take(4)
                    },
                    label = { Text("رمز PIN (4 أرقام)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pinInput.isEmpty() || pinInput.length == 4) {
                        onSavePin(pinInput)
                        onDismiss()
                    }
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            Row {
                if (currentPin.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            onSavePin("")
                            onDismiss()
                        }
                    ) {
                        Text("إلغاء القفل", color = StatusBillRed)
                    }
                }
                TextButton(onClick = onDismiss) { Text("إغلاق") }
            }
        }
    )
}

@Composable
fun PinUnlockOverlay(
    expectedPin: String,
    onUnlocked: () -> Unit,
    onCancel: () -> Unit
) {
    var entered by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.widthIn(max = 360.dp),
            shape = RoundedCornerShape(30.dp),
            opacityMultiplier = 1.2f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SquircleIconBadge(
                    iconKey = "sparkles",
                    accentColor = AmberGold,
                    size = 56.dp,
                    iconSize = 28.dp
                )
                Text(
                    text = "منطقة المالك محمية",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = if (isError) "الرمز غير صحيح، حاول مرة أخرى" else "أدخل رمز PIN المكون من 4 أرقام",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isError) StatusBillRed else MaterialTheme.colorScheme.onSurfaceVariant
                )

                // PIN Dots
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    repeat(4) { index ->
                        val filled = index < entered.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (filled) AmberGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
                                )
                        )
                    }
                }

                // Numeric Keypad
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { key ->
                                Surface(
                                    onClick = {
                                        isError = false
                                        when (key) {
                                            "C" -> entered = ""
                                            "DEL" -> if (entered.isNotEmpty()) entered = entered.dropLast(1)
                                            else -> {
                                                if (entered.length < 4) {
                                                    entered += key
                                                    if (entered.length == 4) {
                                                        if (entered == expectedPin) {
                                                            onUnlocked()
                                                        } else {
                                                            isError = true
                                                            entered = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .size(64.dp)
                                        .minimumInteractiveComponentSize()
                                        .testTag("pin_key_$key")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (key == "DEL") {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                                contentDescription = "مسح"
                                            )
                                        } else {
                                            Text(
                                                text = key,
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                TextButton(onClick = onCancel) {
                    Text("العودة إلى الطاولات")
                }
            }
        }
    }
}
