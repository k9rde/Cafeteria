package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class TopItemTuple(
    val itemName: String,
    val categoryName: String,
    val totalQuantity: Int,
    val totalRevenue: Double
)

@Dao
interface CafeDao {
    // Tables
    @Query("SELECT * FROM cafe_tables ORDER BY sortOrder ASC, id ASC")
    fun observeTables(): Flow<List<CafeTableEntity>>

    @Query("SELECT * FROM cafe_tables WHERE id = :tableId")
    suspend fun getTableById(tableId: Int): CafeTableEntity?

    @Query("SELECT COUNT(*) FROM cafe_tables")
    suspend fun getTableCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: CafeTableEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTables(tables: List<CafeTableEntity>)

    @Update
    suspend fun updateTable(table: CafeTableEntity)

    @Query("DELETE FROM cafe_tables WHERE id = :tableId")
    suspend fun deleteTableById(tableId: Int)

    // Categories
    @Query("SELECT * FROM menu_categories ORDER BY sortOrder ASC, id ASC")
    fun observeCategories(): Flow<List<MenuCategoryEntity>>

    @Query("SELECT COUNT(*) FROM menu_categories")
    suspend fun getCategoryCount(): Int

    @Query("SELECT * FROM menu_categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): MenuCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: MenuCategoryEntity): Long

    @Update
    suspend fun updateCategory(category: MenuCategoryEntity)

    @Query("DELETE FROM menu_categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Int)

    // Menu Items
    @Query("SELECT * FROM menu_items ORDER BY id ASC")
    fun observeMenuItems(): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE id = :itemId")
    suspend fun deleteMenuItemById(itemId: Int)

    @Query("DELETE FROM menu_items WHERE categoryId = :categoryId")
    suspend fun deleteMenuItemsByCategory(categoryId: Int)

    // Active Order Items
    @Query("SELECT * FROM order_items ORDER BY id ASC")
    fun observeAllOrderItems(): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE tableId = :tableId ORDER BY id ASC")
    suspend fun getOrderItemsForTable(tableId: Int): List<OrderItemEntity>

    @Query("SELECT * FROM order_items WHERE id = :orderItemId")
    suspend fun getOrderItemById(orderItemId: Int): OrderItemEntity?

    @Query("SELECT * FROM order_items WHERE tableId = :tableId AND menuItemId = :menuItemId AND note = '' LIMIT 1")
    suspend fun findUnnotedOrderItem(tableId: Int, menuItemId: Int): OrderItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItem(item: OrderItemEntity): Long

    @Update
    suspend fun updateOrderItem(item: OrderItemEntity)

    @Query("DELETE FROM order_items WHERE id = :orderItemId")
    suspend fun deleteOrderItemById(orderItemId: Int)

    @Query("DELETE FROM order_items WHERE tableId = :tableId")
    suspend fun clearOrderItemsForTable(tableId: Int)

    @Query("UPDATE order_items SET tableId = :targetTableId WHERE tableId = :sourceTableId")
    suspend fun moveOrderItems(sourceTableId: Int, targetTableId: Int)

    // Daily Sales
    @Query("SELECT * FROM closed_sales ORDER BY closedAtMillis DESC")
    fun observeClosedSales(): Flow<List<ClosedSaleEntity>>

    @Query(
        """
        SELECT itemName, categoryName, SUM(quantity) AS totalQuantity, SUM(lineTotal) AS totalRevenue
        FROM sold_item_logs
        GROUP BY itemName, categoryName
        ORDER BY totalQuantity DESC, totalRevenue DESC
        """
    )
    fun observeTopSoldItems(): Flow<List<TopItemTuple>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosedSale(sale: ClosedSaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoldItemLogs(logs: List<SoldItemLogEntity>)

    @Query("DELETE FROM closed_sales")
    suspend fun clearClosedSales()

    @Query("DELETE FROM sold_item_logs")
    suspend fun clearSoldItemLogs()

    @Transaction
    suspend fun resetDailyShift() {
        clearSoldItemLogs()
        clearClosedSales()
    }
}
