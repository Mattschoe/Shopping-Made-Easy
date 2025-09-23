package weberstudio.app.billigsteprodukter.ui.pages.shoppingList

import android.app.Application
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.shoppingList.ShoppingListRepository
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.data.ShoppingListCrossRef
import weberstudio.app.billigsteprodukter.data.ShoppingListWithProducts
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptRepository
import weberstudio.app.billigsteprodukter.logic.Store
import java.time.LocalDate
import java.time.LocalDateTime

class ShoppingListsViewModel(application: Application): AndroidViewModel(application) {
    private val app = application as ReceiptApp
    private val shoppingListRepo: ShoppingListRepository = app.shoppingListRepository

    val shoppingLists = shoppingListRepo.getAllShoppingLists().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )



    fun addShoppingList(name: String) {
        val nextNumber = shoppingLists.value.size + 1
        val shoppingList = ShoppingList(
            ID = "shoppingList_$nextNumber",
            name = if (name.isBlank()) "Min indkøbsliste $nextNumber" else name,
            createdDate = LocalDate.now()
        )

        viewModelScope.launch {
            shoppingListRepo.insert(shoppingList)
        }
    }
}

class ShoppingListUndermenuViewModel(application: Application): AndroidViewModel(application) {
    private val app = application as ReceiptApp
    private val shoppingListRepo: ShoppingListRepository = app.shoppingListRepository
    private val productRepo: ReceiptRepository = app.receiptRepository

    private val _selectedShoppingListID = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isStoreExpanded = MutableStateFlow<Map<Int, Boolean>>(emptyMap())

    val searchQuery = _searchQuery.asStateFlow()
    val isStoreExpanded = _isStoreExpanded.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedShoppingList = _selectedShoppingListID.flatMapLatest { ID ->
        if (ID == null) flowOf(null)
        else shoppingListRepo.getShoppingListWithProducts(ID)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    val store2ProductsAdded2Store = _selectedShoppingListID.flatMapLatest { listID ->
        if (listID != null) shoppingListRepo.getShoppingListProductsGroupedByStore(listID)
        else flowOf(emptyMap())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    val storeTotals = _selectedShoppingListID.flatMapLatest { listID ->
        if (listID != null) shoppingListRepo.getStoreTotals(listID)
        else flowOf(emptyMap())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    /**
     * Toggles the product to/from selected
     */
    fun toggleProduct(product: Product) {
        viewModelScope.launch {
            _selectedShoppingListID.value?.let { listID ->
                shoppingListRepo.toggleProductChecked(listID, product.databaseID)
            }
        }
    }

    /**
     * Toggles if the store is expanded into a dropdown
     */
    fun toggleStore(store: Store) {
        _isStoreExpanded.update { currentMap ->
            currentMap + (store.ID to (currentMap[store.ID] != true))
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun selectShoppingList(shoppingListID: String) {
        _selectedShoppingListID.value = shoppingListID
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            _selectedShoppingListID.value?.let { listID ->
                val productID = productRepo.addProduct(product)

                val crossRef = ShoppingListCrossRef(
                    shoppingListID = listID,
                    productID = productID,
                    isChecked = false
                )
                shoppingListRepo.insertShoppingListProductCrossRef(crossRef)
            }
        }
    }
}

