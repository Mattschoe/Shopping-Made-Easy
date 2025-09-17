package weberstudio.app.billigsteprodukter.data.shoppingList

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.data.ShoppingListCrossRef
import weberstudio.app.billigsteprodukter.data.ShoppingListWithProducts

class OfflineShoppingListRepository(private val dao: ShoppingListDao) : ShoppingListRepository {
    override suspend fun insert(shoppingList: ShoppingList) {
        dao.insertShoppingList(shoppingList)
    }

    override suspend fun updateShoppingList(shoppingList: ShoppingList) {
        dao.updateShoppingList(shoppingList)
    }

    override suspend fun deleteShoppingList(shoppingList: ShoppingList) {
        dao.deleteShoppingList(shoppingList)
    }

    override fun getAllShoppingLists(): Flow<List<ShoppingList>> {
        return dao.getAllShoppingLists()
    }

    override suspend fun insertShoppingListProductCrossRef(crossRef: ShoppingListCrossRef) {
        dao.insertShoppingListProductCrossRef(crossRef)
    }

    override fun getShoppingListWithProducts(ID: String): Flow<ShoppingListWithProducts?> {
        return dao.getShoppingListWithProducts(ID)
    }

    override suspend fun removeProductFromShoppingList(shoppingListID: String, productID: Long) {
        dao.removeProductFromShoppingList(shoppingListID, productID)
    }

    override fun getProductCountInShoppingList(shoppingListID: String): Flow<Int> {
        return dao.getProductCountInShoppingList(shoppingListID)
    }

}