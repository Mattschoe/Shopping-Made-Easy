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


    /**
     * SHOULD NOT BE USED, USE [deleteList] INSTEAD!
     */
    @Query("""
        DELETE FROM products WHERE databaseID < 0 AND databaseID IN (
        SELECT productID 
        FROM shopping_list_products 
        WHERE shoppingListID = :shoppingListId)
    """)
    suspend fun deleteCustomProducts(shoppingListId: String)

    /**
     * SHOULD NOT BE USED, USE [deleteList] INSTEAD!
     */
    @Query("DELETE FROM shopping_list WHERE ID = :shoppingListID")
    suspend fun deleteShoppingList(shoppingListID: String)

    /**
     * Also deletes the custom products that are associated with this list
     */
    @Transaction
    suspend fun deleteList(shoppingListID: String) {
        deleteCustomProducts(shoppingListID)
        deleteShoppingList(shoppingListID)
    }

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
    fun getProductCountInShoppingList(shoppingListID: String): Flow<Int>

    @Query("UPDATE shopping_list_products SET isChecked = NOT isChecked WHERE shoppingListID = :shoppingListID AND productID = :productID")
    suspend fun toggleProductChecked(shoppingListID: String, productID: Long)

    @Query("""
    SELECT p.*, slp.isChecked 
    FROM shopping_list_products slp 
    INNER JOIN products p ON slp.productID = p.databaseID 
    WHERE slp.shoppingListID = :shoppingListID
    ORDER BY p.store, p.name
""")
    fun getShoppingListProductsWithCheckedStatus(shoppingListID: String): Flow<List<ProductWithCheckedStatus>>

    @Query("SELECT EXISTS(SELECT 1 FROM shopping_list_products WHERE shoppingListID = :shoppingListID AND productID = :productID)")
    suspend fun isProductInShoppingList(shoppingListID: String, productID: Long): Boolean

    @Query("SELECT databaseID FROM products WHERE databaseID = (SELECT MIN(databaseID) FROM products)")
    suspend fun getLastNegativeID(): Long
}