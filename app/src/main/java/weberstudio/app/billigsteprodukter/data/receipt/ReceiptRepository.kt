package weberstudio.app.billigsteprodukter.data.receipt

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.Receipt
import weberstudio.app.billigsteprodukter.data.ReceiptWithProducts
import weberstudio.app.billigsteprodukter.logic.Store
import java.time.Month
import java.time.Year

interface ReceiptRepository {
    //region STREAMS
    /**
     * Stream of the last receipt received.
     */
    val lastReceipt: Flow<List<Product>>
    //endregion

    //region RECEIPT OPERATIONS
    /**
     * Adds the products from a parsed receipt into the database. Creates a new receipt and associates all products with it.
     * @param receiptTotal the total price read from the receipt
     */
    suspend fun addReceiptProducts(receipt: Receipt, products: Set<Product>): Long

    /**
     * Get receipts within a date range for budget tracking.
     */
    suspend fun getReceiptsForMonth(month: Month, year: Year): Flow<List<ReceiptWithProducts>>

    /**
     * Get a specific receipt with all its products.
     */
    suspend fun getReceiptWithProducts(receiptId: Long): ReceiptWithProducts?
    //endregion

    //region PRODUCT OPERATION
    /**
     * Updates one product, useful if user has corrected a product's price or name.
     */
    suspend fun updateProduct(product: Product)

    /**
     * Adds an extra product to the repo and to the current receipt if it's part of the same store as the lastReceipt.
     * @return false if stores don't match
     */
    suspend fun addProductToCurrentReceipt(product: Product): Boolean

    /**
     * Adds a product to the database
     */
    suspend fun addProduct(product: Product): Long

    /**
     * Remove one product from the database.
     */
    suspend fun removeProduct(product: Product)

    /**
     * Returns all products from the specified store.
     */
    fun getProductsByStore(store: Store): Flow<List<Product>>

    /**
     * Set a product's favorite status using its business ID.
     */
    suspend fun setProductFavorite(store: Store, name: String, isFavorite: Boolean)

    /**
     * Get all favorite products.
     */
    fun getFavoriteProducts(): Flow<List<Product>>

    /**
     * Searches for products in the whole database, based on the query given as argument
     */
    fun searchProductsContaining(query: String): Flow<List<Product>>

    /**
     * Searches for products with the query, in the store given as argument.
     */
    fun searchProductsByStoreContaining(store: Store, query: String): Flow<List<Product>>
    //endregion
}