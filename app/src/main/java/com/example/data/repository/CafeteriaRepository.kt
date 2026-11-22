package com.example.data.repository

import com.example.data.local.CafeDao
import com.example.data.local.CafeTableEntity
import com.example.data.local.ClosedSaleEntity
import com.example.data.local.MenuCategoryEntity
import com.example.data.local.MenuItemEntity
import com.example.data.local.OrderItemEntity
import com.example.data.local.SettingsDataStore
import com.example.data.local.SoldItemLogEntity
import com.example.data.local.TopItemTuple
import com.example.data.model.AppSettingsState
import com.example.data.model.CurrencyOption
import com.example.data.model.TableStatus
import com.example.data.model.WallpaperOption
import com.example.data.model.formatPrice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CafeteriaRepository(
    private val dao: CafeDao,
    private val settingsDataStore: SettingsDataStore
) {
    private val seedMutex = Mutex()
    @Volatile
    private var seeded = false

    val tablesFlow: Flow<List<CafeTableEntity>> = dao.observeTables()
    val categoriesFlow: Flow<List<MenuCategoryEntity>> = dao.observeCategories()
    val menuItemsFlow: Flow<List<MenuItemEntity>> = dao.observeMenuItems()
    val allOrderItemsFlow: Flow<List<OrderItemEntity>> = dao.observeAllOrderItems()
    val closedSalesFlow: Flow<List<ClosedSaleEntity>> = dao.observeClosedSales()
    val topSoldItemsFlow: Flow<List<TopItemTuple>> = dao.observeTopSoldItems()
    val settingsFlow: Flow<AppSettingsState> = settingsDataStore.settingsFlow

    suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return
            if (dao.getTableCount() == 0) {
                val defaultTables = (1..12).map { num ->
                    CafeTableEntity(
                        name = "ميز $num",
                        sortOrder = num,
                        status = TableStatus.AVAILABLE.name
                    )
                }
                dao.insertTables(defaultTables)
            }

            if (dao.getCategoryCount() == 0) {
                val hookahId = dao.insertCategory(
                    MenuCategoryEntity(
                        name = "الأراكيل",
                        iconKey = "hookah",
                        colorHex = 0xFFFF9F0A,
                        sortOrder = 1
                    )
                ).toInt()
                val hotDrinksId = dao.insertCategory(
                    MenuCategoryEntity(
                        name = "المشروبات الساخنة",
                        iconKey = "hot_drink",
                        colorHex = 0xFFBF5AF2,
                        sortOrder = 2
                    )
                ).toInt()
                val coldDrinksId = dao.insertCategory(
                    MenuCategoryEntity(
                        name = "المشروبات الباردة",
                        iconKey = "cold_drink",
                        colorHex = 0xFF32ADE6,
                        sortOrder = 3
                    )
                ).toInt()
                val dessertsId = dao.insertCategory(
                    MenuCategoryEntity(
                        name = "الحلويات والمقبلات",
                        iconKey = "dessert",
                        colorHex = 0xFFFF375F,
                        sortOrder = 4
                    )
                ).toInt()

                dao.insertMenuItems(
                    listOf(
                        // الأراكيل
                        MenuItemEntity(categoryId = hookahId, name = "أركيلة تفاحتين فاخر", price = 6000.0, iconKey = "hookah"),
                        MenuItemEntity(categoryId = hookahId, name = "أركيلة ليمون ونعناع", price = 6000.0, iconKey = "flame"),
                        MenuItemEntity(categoryId = hookahId, name = "أركيلة عنب ونعناع", price = 6000.0, iconKey = "sparkles"),
                        MenuItemEntity(categoryId = hookahId, name = "أركيلة علك ونعناع", price = 6000.0, iconKey = "hookah"),
                        MenuItemEntity(categoryId = hookahId, name = "أركيلة بلوبيري", price = 7000.0, iconKey = "star"),
                        MenuItemEntity(categoryId = hookahId, name = "تبديل رأس أركيلة", price = 3000.0, iconKey = "flame"),
                        // المشروبات الساخنة
                        MenuItemEntity(categoryId = hotDrinksId, name = "شاي عراقي مهيل", price = 1000.0, iconKey = "tea"),
                        MenuItemEntity(categoryId = hotDrinksId, name = "قهوة تركية", price = 2500.0, iconKey = "coffee"),
                        MenuItemEntity(categoryId = hotDrinksId, name = "إسبريسو دبل", price = 3000.0, iconKey = "hot_drink"),
                        MenuItemEntity(categoryId = hotDrinksId, name = "كابتشينو", price = 3500.0, iconKey = "coffee"),
                        MenuItemEntity(categoryId = hotDrinksId, name = "كافيه لاتيه", price = 3500.0, iconKey = "hot_drink"),
                        MenuItemEntity(categoryId = hotDrinksId, name = "حليب بالهيل والزعفران", price = 2500.0, iconKey = "tea"),
                        // المشروبات الباردة
                        MenuItemEntity(categoryId = coldDrinksId, name = "موهيتو كلاسيك", price = 4000.0, iconKey = "cold_drink"),
                        MenuItemEntity(categoryId = coldDrinksId, name = "موهيتو فراولة", price = 4500.0, iconKey = "juice"),
                        MenuItemEntity(categoryId = coldDrinksId, name = "عصير برتقال طازج", price = 3500.0, iconKey = "juice"),
                        MenuItemEntity(categoryId = coldDrinksId, name = "ليمون بالنعناع", price = 3500.0, iconKey = "cold_drink"),
                        MenuItemEntity(categoryId = coldDrinksId, name = "آيس أمريكانو", price = 4000.0, iconKey = "coffee"),
                        MenuItemEntity(categoryId = coldDrinksId, name = "مياه معدنية", price = 500.0, iconKey = "cold_drink"),
                        // الحلويات والمقبلات
                        MenuItemEntity(categoryId = dessertsId, name = "كنافة بالقشطة", price = 5000.0, iconKey = "dessert"),
                        MenuItemEntity(categoryId = dessertsId, name = "تشيز كيك لوتس", price = 5500.0, iconKey = "cake"),
                        MenuItemEntity(categoryId = dessertsId, name = "كريب نوتيلا وموز", price = 6000.0, iconKey = "dessert"),
                        MenuItemEntity(categoryId = dessertsId, name = "مكسرات مشكلة", price = 3500.0, iconKey = "snack"),
                        MenuItemEntity(categoryId = dessertsId, name = "فواكه موسمية", price = 5000.0, iconKey = "sparkles")
                    )
                )
            }
            seeded = true
        }
    }

    // Table Management
    suspend fun addTable(customName: String? = null) {
        val nextNum = dao.getTableCount() + 1
        val name = customName?.takeIf { it.isNotBlank() } ?: "ميز $nextNum"
        dao.insertTable(
            CafeTableEntity(
                name = name,
                sortOrder = nextNum,
                status = TableStatus.AVAILABLE.name
            )
        )
    }

    suspend fun renameTable(tableId: Int, newName: String) {
        val cleanName = newName.trim()
        if (cleanName.isEmpty()) return
        val table = dao.getTableById(tableId) ?: return
        dao.updateTable(table.copy(name = cleanName))
    }

    suspend fun deleteTable(tableId: Int) {
        dao.clearOrderItemsForTable(tableId)
        dao.deleteTableById(tableId)
    }

    suspend fun toggleBillRequested(tableId: Int) {
        val table = dao.getTableById(tableId) ?: return
        val current = table.statusEnum
        val nextStatus = when (current) {
            TableStatus.OCCUPIED -> TableStatus.BILL_REQUESTED
            TableStatus.BILL_REQUESTED -> TableStatus.OCCUPIED
            TableStatus.AVAILABLE -> TableStatus.AVAILABLE
        }
        dao.updateTable(table.copy(status = nextStatus.name))
    }

    // Order Operations
    suspend fun addMenuItemToTable(tableId: Int, menuItem: MenuItemEntity) {
        val table = dao.getTableById(tableId) ?: return
        val category = dao.getCategoryById(menuItem.categoryId)
        val existing = dao.findUnnotedOrderItem(tableId, menuItem.id)
        if (existing != null) {
            dao.updateOrderItem(existing.copy(quantity = existing.quantity + 1))
        } else {
            dao.insertOrderItem(
                OrderItemEntity(
                    tableId = tableId,
                    menuItemId = menuItem.id,
                    itemName = menuItem.name,
                    categoryName = category?.name ?: "أخرى",
                    iconKey = menuItem.iconKey,
                    unitPrice = menuItem.price,
                    quantity = 1,
                    note = ""
                )
            )
        }

        if (table.statusEnum == TableStatus.AVAILABLE) {
            dao.updateTable(
                table.copy(
                    status = TableStatus.OCCUPIED.name,
                    occupiedSinceMillis = table.occupiedSinceMillis ?: System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun incrementOrderItem(orderItemId: Int) {
        val item = dao.getOrderItemById(orderItemId) ?: return
        dao.updateOrderItem(item.copy(quantity = item.quantity + 1))
    }

    suspend fun decrementOrderItem(orderItemId: Int) {
        val item = dao.getOrderItemById(orderItemId) ?: return
        if (item.quantity > 1) {
            dao.updateOrderItem(item.copy(quantity = item.quantity - 1))
        } else {
            dao.deleteOrderItemById(orderItemId)
            val remaining = dao.getOrderItemsForTable(item.tableId)
            if (remaining.isEmpty()) {
                val table = dao.getTableById(item.tableId)
                if (table != null) {
                    dao.updateTable(
                        table.copy(
                            status = TableStatus.AVAILABLE.name,
                            occupiedSinceMillis = null,
                            discountPercent = 0.0,
                            serviceCharge = 0.0
                        )
                    )
                }
            }
        }
    }

    suspend fun updateOrderItemNote(orderItemId: Int, note: String) {
        val item = dao.getOrderItemById(orderItemId) ?: return
        dao.updateOrderItem(item.copy(note = note.trim()))
    }

    suspend fun updateTableCharges(tableId: Int, discountPercent: Double, serviceCharge: Double) {
        val table = dao.getTableById(tableId) ?: return
        dao.updateTable(
            table.copy(
                discountPercent = discountPercent.coerceIn(0.0, 100.0),
                serviceCharge = serviceCharge.coerceAtLeast(0.0)
            )
        )
    }

    suspend fun transferTable(fromTableId: Int, toTableId: Int): Boolean {
        if (fromTableId == toTableId) return false
        val fromTable = dao.getTableById(fromTableId) ?: return false
        val toTable = dao.getTableById(toTableId) ?: return false
        val items = dao.getOrderItemsForTable(fromTableId)
        if (items.isEmpty()) return false

        dao.moveOrderItems(fromTableId, toTableId)
        dao.updateTable(
            toTable.copy(
                status = fromTable.status,
                occupiedSinceMillis = fromTable.occupiedSinceMillis ?: System.currentTimeMillis(),
                discountPercent = fromTable.discountPercent,
                serviceCharge = fromTable.serviceCharge
            )
        )
        dao.updateTable(
            fromTable.copy(
                status = TableStatus.AVAILABLE.name,
                occupiedSinceMillis = null,
                discountPercent = 0.0,
                serviceCharge = 0.0
            )
        )
        return true
    }

    suspend fun checkoutAndClearTable(tableId: Int): Boolean {
        val table = dao.getTableById(tableId) ?: return false
        val items = dao.getOrderItemsForTable(tableId)
        if (items.isEmpty()) {
            dao.updateTable(
                table.copy(
                    status = TableStatus.AVAILABLE.name,
                    occupiedSinceMillis = null,
                    discountPercent = 0.0,
                    serviceCharge = 0.0
                )
            )
            return true
        }

        val subtotal = items.sumOf { it.lineTotal }
        val discountAmount = subtotal * (table.discountPercent / 100.0)
        val grandTotal = (subtotal - discountAmount + table.serviceCharge).coerceAtLeast(0.0)
        val now = System.currentTimeMillis()

        val saleId = dao.insertClosedSale(
            ClosedSaleEntity(
                tableName = table.name,
                subtotal = subtotal,
                discountAmount = discountAmount,
                serviceCharge = table.serviceCharge,
                grandTotal = grandTotal,
                closedAtMillis = now
            )
        ).toInt()

        dao.insertSoldItemLogs(
            items.map { item ->
                SoldItemLogEntity(
                    saleId = saleId,
                    itemName = item.itemName,
                    categoryName = item.categoryName,
                    quantity = item.quantity,
                    lineTotal = item.lineTotal,
                    closedAtMillis = now
                )
            }
        )

        dao.clearOrderItemsForTable(tableId)
        dao.updateTable(
            table.copy(
                status = TableStatus.AVAILABLE.name,
                occupiedSinceMillis = null,
                discountPercent = 0.0,
                serviceCharge = 0.0
            )
        )
        return true
    }

    // Category & Menu Item Management
    suspend fun saveCategory(id: Int?, name: String, iconKey: String, colorHex: Long) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        if (id == null || id == 0) {
            val order = dao.getCategoryCount() + 1
            dao.insertCategory(
                MenuCategoryEntity(
                    name = cleanName,
                    iconKey = iconKey,
                    colorHex = colorHex,
                    sortOrder = order
                )
            )
        } else {
            val existing = dao.getCategoryById(id) ?: return
            dao.updateCategory(existing.copy(name = cleanName, iconKey = iconKey, colorHex = colorHex))
        }
    }

    suspend fun deleteCategory(categoryId: Int) {
        dao.deleteMenuItemsByCategory(categoryId)
        dao.deleteCategoryById(categoryId)
    }

    suspend fun saveMenuItem(
        id: Int?,
        categoryId: Int,
        name: String,
        price: Double,
        iconKey: String
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty() || price < 0.0) return
        val item = MenuItemEntity(
            id = id ?: 0,
            categoryId = categoryId,
            name = cleanName,
            price = price,
            iconKey = iconKey,
            isAvailable = true
        )
        if (id == null || id == 0) {
            dao.insertMenuItem(item)
        } else {
            dao.updateMenuItem(item)
        }
    }

    suspend fun deleteMenuItem(itemId: Int) {
        dao.deleteMenuItemById(itemId)
    }

    suspend fun resetDailyShift() {
        dao.resetDailyShift()
    }

    // Settings
    suspend fun updateGlassOpacity(value: Float) = settingsDataStore.updateGlassOpacity(value)
    suspend fun updateBlurIntensity(value: Float) = settingsDataStore.updateBlurIntensity(value)
    suspend fun updateDarkMode(isDark: Boolean) = settingsDataStore.updateDarkMode(isDark)
    suspend fun updateWallpaper(wallpaper: WallpaperOption) = settingsDataStore.updateWallpaper(wallpaper)
    suspend fun updateCurrency(currency: CurrencyOption) = settingsDataStore.updateCurrency(currency)
    suspend fun updateOwnerPin(pin: String) = settingsDataStore.updateOwnerPin(pin)

    fun buildReceiptText(
        table: CafeTableEntity,
        items: List<OrderItemEntity>,
        currency: CurrencyOption
    ): String {
        val timeFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
        val subtotal = items.sumOf { it.lineTotal }
        val discountAmount = subtotal * (table.discountPercent / 100.0)
        val grandTotal = (subtotal - discountAmount + table.serviceCharge).coerceAtLeast(0.0)

        return buildString {
            appendLine("══════ كافتيريا ══════")
            appendLine("الطاولة: ${table.name}")
            appendLine("التاريخ: ${timeFormat.format(Date())}")
            appendLine("──────────────────────")
            items.forEach { item ->
                appendLine("${item.itemName} × ${item.quantity}  —  ${formatPrice(item.lineTotal, currency)}")
                if (item.note.isNotBlank()) {
                    appendLine("   * ملاحظة: ${item.note}")
                }
            }
            appendLine("──────────────────────")
            appendLine("المجموع الفرعي: ${formatPrice(subtotal, currency)}")
            if (discountAmount > 0.0) {
                appendLine("الخصم (${table.discountPercent.toInt()}%): -${formatPrice(discountAmount, currency)}")
            }
            if (table.serviceCharge > 0.0) {
                appendLine("رسوم الخدمة: +${formatPrice(table.serviceCharge, currency)}")
            }
            appendLine("المجموع الكلي: ${formatPrice(grandTotal, currency)}")
            appendLine("══════════════════════")
            appendLine("شكراً لزيارتكم!")
        }
    }
}
