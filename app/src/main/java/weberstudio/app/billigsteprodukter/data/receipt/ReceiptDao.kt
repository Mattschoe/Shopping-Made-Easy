package weberstudio.app.billigsteprodukter.data.receipt

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.Receipt
import weberstudio.app.billigsteprodukter.data.ReceiptWithProducts
import weberstudio.app.billigsteprodukter.logic.Store

@Dao
interface ReceiptDao {
    //region INSERT OPERATIONS
    @Insert
    suspend fun insertReceipt(receipt: Receipt): Long

    @Insert
    suspend fun insertProducts(products: List<Product>)

    @Insert
    suspend fun insertProduct(product: Product): Long

    @Transaction
    suspend fun insertReceiptWithProducts(receipt: Receipt, products: List<Product>): Long {
        val receiptID = insertReceipt(receipt)
        val productsWithReceiptID = products.map { it.copy(receiptID = receiptID) }
        insertProducts(productsWithReceiptID)
        return receiptID
    }
    //endregion

    //region UPDATE OPERATIONS
    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET isFavorite = :isFavorite WHERE store = :store AND name = :name")
    suspend fun setProductFavorite(store: Store, name: String, isFavorite: Boolean)
    //endregion

    //region DELETE OPERATIONS
    @Delete
    suspend fun deleteReceipt(receipt: Receipt)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("DELETE FROM products WHERE store = :store AND name = :name")
    suspend fun deleteProductByBusinessID(store: Store, name: String)
    //endregion

    //region QUERIES
    //region RECEIPT QUERIES
    @Transaction
    @Query("SELECT * FROM receipts WHERE receiptID = :receiptID")
    suspend fun getReceiptWithProducts(receiptID: Long): ReceiptWithProducts?

    @Transaction
    @Query("SELECT * FROM receipts WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getReceiptsBetweenDates(startDate: Long, endDate: Long): Flow<List<ReceiptWithProducts>>

    @Query("""
        UPDATE receipts
        SET total = COALESCE((SELECT SUM(price) FROM products WHERE products.receiptID = receipts.receiptID), 0)
        WHERE store = :store""")
    suspend fun recomputeTotalForReceiptsInStore(store: Store)

    @Transaction
    @Query("SELECT * FROM receipts ORDER BY date DESC")
    fun getAllReceipts(): Flow<List<Receipt>>
    //endregion

    //region PRODUCT QUERIES
    @Query("SELECT * FROM products WHERE databaseID >= 0")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE store = :store AND databaseID >= 0")
    fun getProductsByStore(store: Store): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE receiptID = :receiptID AND databaseID >= 0")
    fun getProductsForReceipt(receiptID: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isFavorite = 1 AND databaseID >= 0")
    fun getFavoriteProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products where store = :store AND name = :name AND databaseID >= 0 LIMIT 1 ")
    suspend fun getProductByBusinessID(store: Store, name: String): Product?

    @Query("SELECT * FROM products WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%' AND databaseID >= 0 LIMIT 100")
    fun searchProductsContaining(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE store = :store AND LOWER(name) LIKE '%' || LOWER(:query) || '%' AND databaseID >= 0 LIMIT 200")
    fun searchProductsByStoreContaining(store: Store, query: String): Flow<List<Product>>
    //endregion

    //region UTILITY
    @Query("SELECT COUNT(*) FROM receipts")
    suspend fun getReceiptCount(): Int

    @Query("SELECT COUNT(*) FROM products WHERE receiptID = :receiptID")
    suspend fun getProductCountForReceipt(receiptID: Long): Int
    //endregion
    //endregion
}