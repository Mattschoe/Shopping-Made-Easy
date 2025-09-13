package weberstudio.app.billigsteprodukter.data.shoppingList

import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.logic.Store

class OfflineShoppingListRepository(private val dao: ShoppingListDao) : ShoppingListRepository {
    override fun saveShoppingList(shoppingList: ShoppingList) {
        TODO("Not yet implemented")
    }

    override fun deleteShoppingList(ID: String) {
        TODO("Not yet implemented")
    }

    override fun getShoppingListByID(ID: String): ShoppingList? {
        TODO("Not yet implemented")
    }

    override fun getAllShoppingLists(): List<ShoppingList> {
        TODO("Not yet implemented")
    }

    override fun updateShoppingList(
        ID: String,
        store2Product: Map<Store, List<Product>>
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getProductsGroupedByStore(listId: String): Map<Store, List<Product>> {
        val products = dao.getProductsGroupedByStore(listId)
        return products.groupBy { it.store }
    }
}