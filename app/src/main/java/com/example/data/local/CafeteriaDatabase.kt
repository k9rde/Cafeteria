package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CafeTableEntity::class,
        MenuCategoryEntity::class,
        MenuItemEntity::class,
        OrderItemEntity::class,
        ClosedSaleEntity::class,
        SoldItemLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CafeteriaDatabase : RoomDatabase() {
    abstract fun cafeDao(): CafeDao

    companion object {
        @Volatile
        private var INSTANCE: CafeteriaDatabase? = null

        fun getInstance(context: Context): CafeteriaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CafeteriaDatabase::class.java,
                    "cafeteria_pos.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
