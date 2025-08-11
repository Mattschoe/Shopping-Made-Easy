package weberstudio.app.billigsteprodukter.ui.pages.shoppingList

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.logic.Store

class ShoppingListViewModel: ViewModel() {
    private val _store2ProductsAdded2Store = MutableStateFlow<Map<Store, List<Product>>>(emptyMap())
    private val _isStoreExpanded = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    private val _selectedProducts = MutableStateFlow<Set<Product>>(emptySet()) //All selected product

    val store2ProductsAdded2Store = _store2ProductsAdded2Store.asStateFlow()
    val isStoreExpanded = _isStoreExpanded.asStateFlow()
    val selectedProducts = _selectedProducts.asStateFlow()


    /**
     * Adds a product to the grocery list
     */
    fun addProduct(productName: String, store: Store) {
        val product = Product(productName, 0.0f, store)
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
     * Updates the flow with the new info.
     */
    private fun updateFlows(store: Store, product: Product) {
        _store2ProductsAdded2Store.update { currentMap ->
            val currentProducts = currentMap[store].orEmpty()
            currentMap.toMutableMap().apply { this[store] = currentProducts + product } //New map with new list with same entries + the new product
        }

        updateIsStoreExpanded(store)
    }

    private fun updateIsStoreExpanded(store: Store, toggle: Boolean = true) {
        _isStoreExpanded.update { currentMap ->
            currentMap.toMutableMap().apply {
                this.getOrPut(store.ID) { toggle } //Adds store to the map
            }
        }
    }
}