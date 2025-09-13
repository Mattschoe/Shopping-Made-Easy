package weberstudio.app.billigsteprodukter.data.shoppingList

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.*

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list ORDER BY createdDate DESC")
    fun getAllShoppingLists(): Flow<List<ShoppingList>>

    @Transaction
    @Query("SELECT * FROM shopping_list WHERE ID = :shoppingListId")
    suspend fun getShoppingListWithProducts(shoppingListId: String): ShoppingListWithProducts?

    @Insert
    suspend fun insertShoppingList(shoppingList: ShoppingList)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addProductToShoppingList(crossRef: ShoppingListCrossRef)

    @Delete
    suspend fun removeProductFromShoppingList(crossRef: ShoppingListCrossRef)

    @Query("DELETE FROM shopping_list WHERE ID = :shoppingListId")
    suspend fun deleteShoppingList(shoppingListId: String)

    @Query("UPDATE shopping_list SET name = :newName WHERE ID = :shoppingListId")
    suspend fun updateShoppingListName(shoppingListId: String, newName: String)

    @Transaction
    @Query("""
        SELECT p.* FROM products p 
        INNER JOIN shopping_list_products slp ON p.databaseID = slp.productID 
        WHERE slp.shoppingListID = :shoppingListId
        ORDER BY p.store, p.name
    """)
    suspend fun getProductsGroupedByStore(shoppingListId: String): List<Product>
}