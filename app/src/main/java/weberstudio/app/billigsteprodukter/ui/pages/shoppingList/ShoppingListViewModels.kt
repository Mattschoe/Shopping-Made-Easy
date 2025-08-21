package weberstudio.app.billigsteprodukter.ui.pages.shoppingList

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import weberstudio.app.billigsteprodukter.data.ShoppingListRepository
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Store
import java.time.LocalDateTime

class ShoppingListsViewModel: ViewModel() {
    private val repo = ShoppingListRepository
    private val _shoppingLists = MutableStateFlow<List<ShoppingList>>(emptyList())

    val shoppingLists = _shoppingLists.asStateFlow()

    init {
        val lists = repo.getAllShoppingLists()
        _shoppingLists.value = lists
    }

    /**
     * Creates a new shopping list with it's undermenu
     * @return ID of the **ShoppingList** created
     */
    fun createShoppingList(name: String): String {
        val nextNumber = _shoppingLists.value.size + 1
        val defaultName = if (name.isBlank()) { //Sets the name of the shopping list as name given as argument, unless given a blank name
            "Min indkøbsliste $nextNumber"
        } else name
        val ID = "shoppingList_$nextNumber"

        val shoppingList = ShoppingList(
            ID = ID,
            name = defaultName,
            store2Products = emptyMap(),
            createdDate = LocalDateTime.now()
        )

        repo.saveShoppingList(shoppingList)
        _shoppingLists.update { currentList -> currentList + shoppingList }
        return shoppingList.ID
    }

    /**
     * Deletes the shopping list from the given **ID**
     * @param ID of the shoppingList to be deleted
     */
    fun deleteShoppingList(ID: String) {
        repo.deleteShoppingList(ID) // Remove from repository

        // Update UI state
        _shoppingLists.update { currentLists ->
            currentLists.filter { it.ID != ID }
        }
    }
}

class ShoppingListUndermenuViewModel: ViewModel() {
    private val repo = ShoppingListRepository
    private var currentShoppingListID: String? = null

    private val _store2ProductsAdded2Store = MutableStateFlow<Map<Store, List<Product>>>(emptyMap())
    private val _isStoreExpanded = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    private val _selectedProducts = MutableStateFlow<Set<Product>>(emptySet()) //All selected product

    val store2ProductsAdded2Store = _store2ProductsAdded2Store.asStateFlow()
    val isStoreExpanded = _isStoreExpanded.asStateFlow()
    val selectedProducts = _selectedProducts.asStateFlow()

    /**
     * Loads a specific shopping list by given ID
     * @param listID the ID of the store wished to be loaded
     */
    fun loadShoppingList(listID: String) {
        currentShoppingListID = listID
        val shoppingList = repo.getShoppingListByID(listID)
        if (shoppingList != null) {
            //Loads shopping list, resets states aswell
            _store2ProductsAdded2Store.value = shoppingList.store2Products
            _selectedProducts.value = emptySet()
            _isStoreExpanded.value = emptyMap()
        } else {
            //TODO: ERROR STATE, NO SHOPPING LIST WITH ID FOUND
        }
    }

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

    /**
     * @return the name of the shopping list from the **ID** given as argument
     */
    fun getShoppingListName(ID: String): String? {
        return repo.getShoppingListByID(ID)?.name
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
        saveCurrentList()
    }

    private fun updateIsStoreExpanded(store: Store, toggle: Boolean = true) {
        _isStoreExpanded.update { currentMap ->
            currentMap.toMutableMap().apply {
                this.getOrPut(store.ID) { toggle } //Adds store to the map
            }
        }
    }

    private fun saveCurrentList() {
        currentShoppingListID?.let { listID ->
            repo.updateShoppingList(listID, _store2ProductsAdded2Store.value)
        }
    }
}

data class ShoppingList(
    val ID: String,
    val name: String,
    val store2Products: Map<Store, List<Product>>,
    val createdDate: LocalDateTime
)