package weberstudio.app.billigsteprodukter.data.shoppingList

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.data.ShoppingListCrossRef
import weberstudio.app.billigsteprodukter.data.ShoppingListWithProducts
import weberstudio.app.billigsteprodukter.logic.Store

interface ShoppingListRepository {
    /**
     * Saves the shopping list to the repository
     */
    suspend fun insert(shoppingList: ShoppingList)

    suspend fun updateShoppingList(shoppingList: ShoppingList)

    suspend fun deleteShoppingList(shoppingList: ShoppingList)

    fun getAllShoppingLists(): Flow<List<ShoppingList>>

    suspend fun insertShoppingListProductCrossRef(crossRef: ShoppingListCrossRef)

    fun getShoppingListWithProducts(ID: String): Flow<ShoppingListWithProducts?>

    suspend fun removeProductFromShoppingList(shoppingListID: String, productID: Long)

    fun getProductCountInShoppingList(shoppingListID: String): Flow<Int>

    suspend fun toggleProductChecked(shoppingListID: String, productID: Long)

    fun getShoppingListProductsGroupedByStore(shoppingListID: String): Flow<Map<Store, List<Pair<Product, Boolean>>>>

    fun getStoreTotals(shoppingListID: String): Flow<Map<Store, Pair<Int, Int>>>
}