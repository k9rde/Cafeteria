package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CafeTableEntity
import com.example.data.local.MenuCategoryEntity
import com.example.data.local.MenuItemEntity
import com.example.data.local.OrderItemEntity
import com.example.data.model.AppSettingsState
import com.example.data.model.CurrencyOption
import com.example.data.model.DailySummaryState
import com.example.data.model.TableFilter
import com.example.data.model.TableStatus
import com.example.data.model.TopSoldItem
import com.example.data.model.WallpaperOption
import com.example.data.repository.CafeteriaRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TableCardUiModel(
    val table: CafeTableEntity,
    val itemCount: Int,
    val subtotal: Double,
    val grandTotal: Double
)

data class CafeteriaUiState(
    val tables: List<TableCardUiModel> = emptyList(),
    val filteredTables: List<TableCardUiModel> = emptyList(),
    val tableFilter: TableFilter = TableFilter.ALL,
    val selectedTableId: Int? = null,
    val selectedTable: TableCardUiModel? = null,
    val selectedTableOrderItems: List<OrderItemEntity> = emptyList(),
    val categories: List<MenuCategoryEntity> = emptyList(),
    val menuItems: List<MenuItemEntity> = emptyList(),
    val selectedCategoryId: Int? = null,
    val dailySummary: DailySummaryState = DailySummaryState(),
    val settings: AppSettingsState = AppSettingsState(),
    val isEditTablesMode: Boolean = false,
    val availableCount: Int = 0,
    val occupiedCount: Int = 0,
    val billRequestedCount: Int = 0
)

class CafeteriaViewModel(
    private val repository: CafeteriaRepository
) : ViewModel() {

    private val _tableFilter = MutableStateFlow(TableFilter.ALL)
    private val _selectedTableId = MutableStateFlow<Int?>(null)
    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    private val _isEditTablesMode = MutableStateFlow(false)

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    val isEditTablesMode: StateFlow<Boolean> = _isEditTablesMode.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private val tablesAndOrdersFlow = combine(
        repository.tablesFlow,
        repository.allOrderItemsFlow
    ) { tables, orderItems ->
        val grouped = orderItems.groupBy { it.tableId }
        tables.map { table ->
            val items = grouped[table.id].orEmpty()
            val itemCount = items.sumOf { it.quantity }
            val subtotal = items.sumOf { it.lineTotal }
            val discount = subtotal * (table.discountPercent / 100.0)
            val grandTotal = (subtotal - discount + table.serviceCharge).coerceAtLeast(0.0)
            TableCardUiModel(
                table = table,
                itemCount = itemCount,
                subtotal = subtotal,
                grandTotal = grandTotal
            )
        }
    }

    private val dailySummaryFlow = combine(
        repository.closedSalesFlow,
        repository.topSoldItemsFlow
    ) { sales, topTuples ->
        val topItems = topTuples.map {
            TopSoldItem(
                itemName = it.itemName,
                categoryName = it.categoryName,
                totalQuantity = it.totalQuantity,
                totalRevenue = it.totalRevenue
            )
        }
        val topHookah = topItems.firstOrNull {
            it.categoryName.contains("أراكيل") || it.itemName.contains("أركيلة")
        }
        val topDrink = topItems.firstOrNull {
            it.categoryName.contains("مشروبات") || it.categoryName.contains("ساخنة") || it.categoryName.contains("باردة")
        }
        DailySummaryState(
            totalRevenue = sales.sumOf { it.grandTotal },
            closedTablesCount = sales.size,
            topItems = topItems,
            topHookah = topHookah,
            topDrink = topDrink
        )
    }

    val uiState: StateFlow<CafeteriaUiState> = combine(
        tablesAndOrdersFlow,
        repository.allOrderItemsFlow,
        repository.categoriesFlow,
        repository.menuItemsFlow,
        dailySummaryFlow,
        repository.settingsFlow,
        _tableFilter,
        _selectedTableId,
        _selectedCategoryId,
        _isEditTablesMode
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val tableModels = values[0] as List<TableCardUiModel>
        @Suppress("UNCHECKED_CAST")
        val allOrders = values[1] as List<OrderItemEntity>
        @Suppress("UNCHECKED_CAST")
        val categories = values[2] as List<MenuCategoryEntity>
        @Suppress("UNCHECKED_CAST")
        val menuItems = values[3] as List<MenuItemEntity>
        val dailySummary = values[4] as DailySummaryState
        val settings = values[5] as AppSettingsState
        val filter = values[6] as TableFilter
        val selectedTableId = values[7] as Int?
        val selectedCategoryId = values[8] as Int?
        val editMode = values[9] as Boolean

        val filtered = when (filter) {
            TableFilter.ALL -> tableModels
            TableFilter.AVAILABLE -> tableModels.filter { it.table.statusEnum == TableStatus.AVAILABLE }
            TableFilter.OCCUPIED -> tableModels.filter { it.table.statusEnum == TableStatus.OCCUPIED }
            TableFilter.BILL_REQUESTED -> tableModels.filter { it.table.statusEnum == TableStatus.BILL_REQUESTED }
        }

        val resolvedTableId = selectedTableId?.takeIf { id -> tableModels.any { it.table.id == id } }
        val selectedTable = tableModels.firstOrNull { it.table.id == resolvedTableId }
        val selectedOrderItems = if (resolvedTableId != null) {
            allOrders.filter { it.tableId == resolvedTableId }
        } else {
            emptyList()
        }
        val resolvedCategoryId = selectedCategoryId?.takeIf { id -> categories.any { it.id == id } }
            ?: categories.firstOrNull()?.id

        CafeteriaUiState(
            tables = tableModels,
            filteredTables = filtered,
            tableFilter = filter,
            selectedTableId = resolvedTableId,
            selectedTable = selectedTable,
            selectedTableOrderItems = selectedOrderItems,
            categories = categories,
            menuItems = menuItems,
            selectedCategoryId = resolvedCategoryId,
            dailySummary = dailySummary,
            settings = settings,
            isEditTablesMode = editMode,
            availableCount = tableModels.count { it.table.statusEnum == TableStatus.AVAILABLE },
            occupiedCount = tableModels.count { it.table.statusEnum == TableStatus.OCCUPIED },
            billRequestedCount = tableModels.count { it.table.statusEnum == TableStatus.BILL_REQUESTED }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CafeteriaUiState()
    )

    fun setTableFilter(filter: TableFilter) {
        _tableFilter.value = filter
    }

    fun selectTable(tableId: Int?) {
        _selectedTableId.value = tableId
    }

    fun selectCategory(categoryId: Int) {
        _selectedCategoryId.value = categoryId
    }

    fun toggleEditTablesMode() {
        _isEditTablesMode.value = !_isEditTablesMode.value
    }

    fun addTable(name: String? = null) {
        viewModelScope.launch {
            repository.addTable(name)
            _events.emit("تمت إضافة الطاولة بنجاح")
        }
    }

    fun renameTable(tableId: Int, newName: String) {
        viewModelScope.launch {
            repository.renameTable(tableId, newName)
        }
    }

    fun deleteTable(tableId: Int) {
        viewModelScope.launch {
            if (_selectedTableId.value == tableId) {
                _selectedTableId.value = null
            }
            repository.deleteTable(tableId)
            _events.emit("تم حذف الطاولة")
        }
    }

    fun toggleBillRequested(tableId: Int) {
        viewModelScope.launch {
            repository.toggleBillRequested(tableId)
        }
    }

    fun addMenuItemToSelectedTable(item: MenuItemEntity) {
        val tableId = _selectedTableId.value ?: return
        viewModelScope.launch {
            repository.addMenuItemToTable(tableId, item)
        }
    }

    fun incrementOrderItem(orderItemId: Int) {
        viewModelScope.launch {
            repository.incrementOrderItem(orderItemId)
        }
    }

    fun decrementOrderItem(orderItemId: Int) {
        viewModelScope.launch {
            repository.decrementOrderItem(orderItemId)
        }
    }

    fun updateOrderItemNote(orderItemId: Int, note: String) {
        viewModelScope.launch {
            repository.updateOrderItemNote(orderItemId, note)
        }
    }

    fun updateTableCharges(tableId: Int, discountPercent: Double, serviceCharge: Double) {
        viewModelScope.launch {
            repository.updateTableCharges(tableId, discountPercent, serviceCharge)
        }
    }

    fun transferTable(fromTableId: Int, toTableId: Int) {
        viewModelScope.launch {
            val ok = repository.transferTable(fromTableId, toTableId)
            if (ok) {
                _selectedTableId.value = toTableId
                _events.emit("تم نقل الطلبات إلى الطاولة الجديدة")
            }
        }
    }

    fun checkoutAndClearTable(tableId: Int) {
        viewModelScope.launch {
            repository.checkoutAndClearTable(tableId)
            _selectedTableId.value = null
            _events.emit("تم تصفية الحساب وإغلاق الميز بنجاح")
        }
    }

    fun buildReceiptText(table: CafeTableEntity, items: List<OrderItemEntity>, currency: CurrencyOption): String {
        return repository.buildReceiptText(table, items, currency)
    }

    // Owner Settings & Menu Management
    fun saveCategory(id: Int?, name: String, iconKey: String, colorHex: Long) {
        viewModelScope.launch {
            repository.saveCategory(id, name, iconKey, colorHex)
            _events.emit("تم حفظ القسم")
        }
    }

    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
            _events.emit("تم حذف القسم")
        }
    }

    fun saveMenuItem(id: Int?, categoryId: Int, name: String, price: Double, iconKey: String) {
        viewModelScope.launch {
            repository.saveMenuItem(id, categoryId, name, price, iconKey)
            _events.emit("تم حفظ الصنف")
        }
    }

    fun deleteMenuItem(itemId: Int) {
        viewModelScope.launch {
            repository.deleteMenuItem(itemId)
            _events.emit("تم حذف الصنف")
        }
    }

    fun resetDailyShift() {
        viewModelScope.launch {
            repository.resetDailyShift()
            _events.emit("تم تصفير الوردية اليومية")
        }
    }

    // Appearance & Preferences
    fun updateGlassOpacity(value: Float) {
        viewModelScope.launch { repository.updateGlassOpacity(value) }
    }

    fun updateBlurIntensity(value: Float) {
        viewModelScope.launch { repository.updateBlurIntensity(value) }
    }

    fun updateDarkMode(isDark: Boolean) {
        viewModelScope.launch { repository.updateDarkMode(isDark) }
    }

    fun updateWallpaper(wallpaper: WallpaperOption) {
        viewModelScope.launch { repository.updateWallpaper(wallpaper) }
    }

    fun updateCurrency(currency: CurrencyOption) {
        viewModelScope.launch { repository.updateCurrency(currency) }
    }

    fun updateOwnerPin(pin: String) {
        viewModelScope.launch {
            repository.updateOwnerPin(pin)
            _events.emit(if (pin.isBlank()) "تم إلغاء قفل رمز PIN" else "تم حفظ رمز PIN للمالك")
        }
    }
}

class CafeteriaViewModelFactory(
    private val repository: CafeteriaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CafeteriaViewModel::class.java)) {
            return CafeteriaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
