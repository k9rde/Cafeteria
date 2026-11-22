package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.TableStatus

@Entity(tableName = "cafe_tables")
data class CafeTableEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sortOrder: Int = 0,
    val status: String = TableStatus.AVAILABLE.name,
    val occupiedSinceMillis: Long? = null,
    val discountPercent: Double = 0.0,
    val serviceCharge: Double = 0.0
) {
    val statusEnum: TableStatus
        get() = runCatching { TableStatus.valueOf(status) }.getOrDefault(TableStatus.AVAILABLE)
}

@Entity(tableName = "menu_categories")
data class MenuCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconKey: String,
    val colorHex: Long,
    val sortOrder: Int = 0
)

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,
    val name: String,
    val price: Double,
    val iconKey: String,
    val isAvailable: Boolean = true
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tableId: Int,
    val menuItemId: Int,
    val itemName: String,
    val categoryName: String,
    val iconKey: String,
    val unitPrice: Double,
    val quantity: Int,
    val note: String = ""
) {
    val lineTotal: Double get() = unitPrice * quantity
}

@Entity(tableName = "closed_sales")
data class ClosedSaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tableName: String,
    val subtotal: Double,
    val discountAmount: Double,
    val serviceCharge: Double,
    val grandTotal: Double,
    val closedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "sold_item_logs")
data class SoldItemLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val saleId: Int,
    val itemName: String,
    val categoryName: String,
    val quantity: Int,
    val lineTotal: Double,
    val closedAtMillis: Long = System.currentTimeMillis()
)
