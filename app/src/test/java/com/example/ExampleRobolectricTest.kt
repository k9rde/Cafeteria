package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.CafeteriaDatabase
import com.example.data.local.SettingsDataStore
import com.example.data.model.CurrencyOption
import com.example.data.model.TableStatus
import com.example.data.model.formatPrice
import com.example.data.repository.CafeteriaRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: CafeteriaDatabase
    private lateinit var repository: CafeteriaRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, CafeteriaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CafeteriaRepository(db.cafeDao(), SettingsDataStore(context))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app name matches Cafeteria Arabic title`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("كافتيريا", appName)
    }

    @Test
    fun `initial seeding populates tables categories and menu items`() = runTest {
        repository.ensureSeeded()
        val tables = repository.tablesFlow.first()
        val categories = repository.categoriesFlow.first()
        val items = repository.menuItemsFlow.first()

        assertEquals(12, tables.size)
        assertEquals(4, categories.size)
        assertTrue(items.size >= 20)
    }

    @Test
    fun `order addition transfer and checkout flow updates daily summary`() = runTest {
        repository.ensureSeeded()
        val tables = repository.tablesFlow.first()
        val menuItems = repository.menuItemsFlow.first()
        val table1 = tables[0]
        val table2 = tables[1]
        val shisha = menuItems.first()

        // Add item to table 1
        repository.addMenuItemToTable(table1.id, shisha)
        repository.addMenuItemToTable(table1.id, shisha)

        var updatedTables = repository.tablesFlow.first()
        assertEquals(TableStatus.OCCUPIED, updatedTables.first { it.id == table1.id }.statusEnum)

        // Transfer from table 1 to table 2
        val transferred = repository.transferTable(table1.id, table2.id)
        assertTrue(transferred)

        updatedTables = repository.tablesFlow.first()
        assertEquals(TableStatus.AVAILABLE, updatedTables.first { it.id == table1.id }.statusEnum)
        assertEquals(TableStatus.OCCUPIED, updatedTables.first { it.id == table2.id }.statusEnum)

        // Checkout table 2
        val checkedOut = repository.checkoutAndClearTable(table2.id)
        assertTrue(checkedOut)

        updatedTables = repository.tablesFlow.first()
        assertEquals(TableStatus.AVAILABLE, updatedTables.first { it.id == table2.id }.statusEnum)

        val sales = repository.closedSalesFlow.first()
        assertEquals(1, sales.size)
        assertEquals(shisha.price * 2, sales.first().grandTotal, 0.01)

        // Reset shift
        repository.resetDailyShift()
        assertEquals(0, repository.closedSalesFlow.first().size)
    }

    @Test
    fun `currency formatting formats IQD and USD properly`() {
        assertEquals("6,000 د.ع", formatPrice(6000.0, CurrencyOption.IQD))
        assertEquals("$12.50", formatPrice(12.5, CurrencyOption.USD))
    }

    @Test
    fun `adding table after deletion avoids duplicate names and formats decimal discounts`() = runTest {
        repository.ensureSeeded()
        val initialTables = repository.tablesFlow.first()
        val table3 = initialTables.first { it.name == "ميز 3" }

        repository.deleteTable(table3.id)
        repository.addTable()

        val afterAdd = repository.tablesFlow.first()
        assertEquals(12, afterAdd.size)
        assertEquals(12, afterAdd.map { it.name }.distinct().size)
        assertTrue(afterAdd.any { it.name == "ميز 3" })
        assertEquals("10", com.example.data.model.formatDiscountPercent(10.0))
        assertEquals("12.5", com.example.data.model.formatDiscountPercent(12.5))
    }
}
