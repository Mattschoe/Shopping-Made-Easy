package weberstudio.app.billigsteprodukter.data.shoppingList

import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingList

object ShoppingListRepository {


    /**
     * Saves the shopping list to the repository
     */
    fun saveShoppingList(shoppingList: ShoppingList) {
        /* NO-OP */
    }

    fun deleteShoppingList(ID: String) {
        /* NO-OP */
    }

    fun getShoppingListByID(ID: String): ShoppingList? {
        /* NO-OP */
        return null
    }

    fun getAllShoppingLists(): List<ShoppingList> {
        /* NO-OP */
        return emptyList()
    }

    fun updateShoppingList(ID: String, store2Product: Map<Store, List<Product>>) {
        /* NO-OP */
    }
}