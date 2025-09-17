package weberstudio.app.billigsteprodukter.data.shoppingList

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.*

@Dao
interface ShoppingListDao {
    @Insert
    suspend fun insertShoppingList(shoppingList: ShoppingList)

    @Update
    suspend fun updateShoppingList(shoppingList: ShoppingList)

    @Query("SELECT * FROM shopping_list ORDER BY createdDate DESC")
    fun getAllShoppingLists(): Flow<List<ShoppingList>>

    @Delete
    suspend fun deleteShoppingList(shoppingList: ShoppingList)

    @Transaction
    @Query("SELECT * FROM shopping_list ORDER BY createdDate DESC")
    fun getAllShoppingListsWithProducts(): Flow<List<ShoppingListWithProducts>>

    @Transaction
    @Query("SELECT * FROM shopping_list WHERE ID = :ID")
    fun getShoppingListWithProducts(ID: String): Flow<ShoppingListWithProducts?>

    @Insert
    suspend fun insertShoppingListProductCrossRef(crossRef: ShoppingListCrossRef)

    @Query("DELETE FROM shopping_list_products WHERE shoppingListID = :shoppingListID AND productID = :productId")
    suspend fun removeProductFromShoppingList(shoppingListID: String, productId: Long)

    @Query("SELECT COUNT(*) FROM shopping_list_products WHERE shoppingListID = :shoppingListID")
    suspend fun getProductCountInShoppingList(shoppingListID: String): Flow<Int>
}