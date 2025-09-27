package weberstudio.app.billigsteprodukter.data.shoppingList

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.data.ShoppingListCrossRef
import weberstudio.app.billigsteprodukter.data.ShoppingListWithProducts
import weberstudio.app.billigsteprodukter.logic.Store

class OfflineShoppingListRepository(private val dao: ShoppingListDao) : ShoppingListRepository {
    override suspend fun insert(shoppingList: ShoppingList) {
        dao.insertShoppingList(shoppingList)
    }

    override suspend fun updateShoppingList(shoppingList: ShoppingList) {
        dao.updateShoppingList(shoppingList)
    }

    override suspend fun deleteShoppingList(listID: String) {
        dao.deleteList(listID)
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

    override suspend fun toggleProductChecked(shoppingListID: String, productID: Long) {
        dao.toggleProductChecked(shoppingListID, productID)
    }

    override fun getShoppingListProductsGroupedByStore(shoppingListID: String): Flow<Map<Store, List<Pair<Product, Boolean>>>> {
        return dao.getShoppingListProductsWithCheckedStatus(shoppingListID).map { productsWithStatus ->
            productsWithStatus
                .groupBy { it.product.store }
                .mapValues { (_, products) ->
                    products.map { it.product to it.isChecked }
                }
        }
    }

    override fun getStoreTotals(shoppingListID: String): Flow<Map<Store, Pair<Int, Int>>> {
        return dao.getShoppingListProductsWithCheckedStatus(shoppingListID)
            .map { productsWithStatus ->
                productsWithStatus
                    .groupBy { it.product.store }
                    .mapValues { (_, products) ->
                        val total = products.size
                        val checkedOff = products.count { it.isChecked }
                        Pair(total, checkedOff)
                    }
            }
    }

    override suspend fun isProductInShoppingList(listID: String, productID: Long): Boolean {
        return dao.isProductInShoppingList(listID, productID)
    }

    override suspend fun getLastNegativeID(): Long {
        return dao.getLastNegativeID()
    }


}