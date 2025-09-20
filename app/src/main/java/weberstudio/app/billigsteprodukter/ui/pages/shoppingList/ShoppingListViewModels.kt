package weberstudio.app.billigsteprodukter.ui.pages.shoppingList

import android.app.Application
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.shoppingList.ShoppingListRepository
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.ShoppingList
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

    private val _store2ProductsAdded2Store = MutableStateFlow<Map<Store, List<Product>>>(emptyMap())
    private val _isStoreExpanded = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    private val _selectedProducts = MutableStateFlow<Set<Product>>(emptySet()) //All selected product

    val store2ProductsAdded2Store = _store2ProductsAdded2Store.asStateFlow()
    val isStoreExpanded = _isStoreExpanded.asStateFlow()
    val selectedProducts = _selectedProducts.asStateFlow()

    private val _selectedShoppingListID = MutableStateFlow<String?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedShoppingList = _selectedShoppingListID.flatMapLatest { ID ->
        if (ID == null) flowOf(null)
        else shoppingListRepo.getShoppingListWithProducts(ID)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )


    /**
     * Adds a product to the grocery list
     */
    fun addProduct(productName: String, store: Store) {
        val product = Product(name = productName, price =  0.0f, store = store)
        updateFlows(store, product)
    }

    /**
     * Adds a product to the grocery list
     */
    fun addProduct(product: Product, store: Store) {
        updateFlows(store, product)
    }

    /**
     * Toggles if the store is expanded into a dropdown
     */
    fun toggleStore(store: Store, toggle: Boolean) {
        updateIsStoreExpanded(store, toggle)
    }

    /**
     * Toggles the product to/from selected
     */
    fun toggleProduct(product: Product) {
        _selectedProducts.update { currentSet ->
            if (currentSet.contains(product)) currentSet - product else currentSet + product
        }
    }

    /**
     * @return Pair<Total, CheckedOff>
     */
    fun getTotalAndCheckedOff(fromStore: Store): Pair<Int, Int> {
        val storeProductList = _store2ProductsAdded2Store.value.get(fromStore)
        if (storeProductList != null) return Pair(storeProductList.size, storeProductList.count { _selectedProducts.value.contains(it) })
        Log.d("ERROR", "Cant retrieve total and checkedOff from $fromStore")
        return Pair(0,0)
    }

    private fun updateIsStoreExpanded(store: Store, toggle: Boolean = true) {
        _isStoreExpanded.update { currentMap ->
            currentMap.toMutableMap().apply {
                this.getOrPut(store.ID) { toggle } //Adds store to the map
            }
        }
    }

    fun selectShoppingList(shoppingListID: String) {
        _selectedShoppingListID.value = shoppingListID
    }

    /**
     * Removes the product given from the productID from the current selectedShoppingList
     */
    fun removeProduct(productID: Long) {
        val shoppingListID = _selectedShoppingListID.value ?: return
        viewModelScope.launch {
            shoppingListRepo.removeProductFromShoppingList(shoppingListID, productID)
        }
    }

    /**
     * Updates the flow with the new info.
     */
    private fun updateFlows(store: Store, product: Product) {
        _store2ProductsAdded2Store.update { currentMap ->
            val currentProducts = currentMap[store].orEmpty()
            currentMap.toMutableMap().apply { this[store] = currentProducts + product } //New map with new list with same entries + the new product
        }

        updateIsStoreExpanded(store)
    }
}

