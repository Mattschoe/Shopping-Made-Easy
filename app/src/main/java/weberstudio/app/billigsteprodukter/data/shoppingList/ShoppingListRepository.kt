package weberstudio.app.billigsteprodukter.data.shoppingList

import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.logic.Store

interface ShoppingListRepository {
    /**
     * Saves the shopping list to the repository
     */
    fun saveShoppingList(shoppingList: ShoppingList)

    fun deleteShoppingList(ID: String)

    fun getShoppingListByID(ID: String): ShoppingList?

    fun getAllShoppingLists(): List<ShoppingList>

    fun updateShoppingList(ID: String, store2Product: Map<Store, List<Product>>)

    suspend fun getProductsGroupedByStore(listId: String): Map<Store, List<Product>>

}