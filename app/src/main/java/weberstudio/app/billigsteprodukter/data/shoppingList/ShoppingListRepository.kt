package weberstudio.app.billigsteprodukter.data.shoppingList

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.data.ShoppingListCrossRef
import weberstudio.app.billigsteprodukter.data.ShoppingListWithProducts

interface ShoppingListRepository {
    /**
     * Saves the shopping list to the repository
     */
    suspend fun insert(shoppingList: ShoppingList)

    suspend fun updateShoppingList(shoppingList: ShoppingList)

    suspend fun deleteShoppingList(shoppingList: ShoppingList)

    suspend fun getAllShoppingLists(): Flow<List<ShoppingList>>

    suspend fun insertShoppingListProductCrossRef(crossRef: ShoppingListCrossRef)

    suspend fun getShoppingListWithProducts(ID: String): Flow<ShoppingListWithProducts?>

    suspend fun removeProductFromShoppingList(shoppingListID: String, productID: Long)

    suspend fun getProductCountInShoppingList(shoppingListID: String): Flow<Int>
}