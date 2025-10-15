package weberstudio.app.billigsteprodukter.data.receipt

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.Receipt
import weberstudio.app.billigsteprodukter.data.ReceiptWithProducts
import weberstudio.app.billigsteprodukter.logic.Store
import java.time.Month
import java.time.Year

interface ReceiptRepository {
    /**
     * Adds a receipt with its products to the database.
     * @return The ID of the newly created receipt
     */
    suspend fun addReceiptProducts(receipt: Receipt, products: Set<Product>): Long

    /**
     * Deletes a receipt and all its associated products.
     */
    suspend fun deleteReceipt(receipt: Receipt)

    /**
     * Gets a receipt with all its products (one-time read).
     * For observing changes, use getProductsForReceipt() instead.
     */
    suspend fun getReceiptWithProducts(receiptID: Long): Flow<ReceiptWithProducts?>

    /**
     * Gets all receipts for a specific month.
     * @return Flow that emits whenever receipts change
     */
    suspend fun getReceiptsForMonth(month: Month, year: Year): Flow<List<ReceiptWithProducts>>

    /**
     * Recomputes the total for all receipts in a store.
     * Useful after bulk price updates.
     */
    suspend fun recomputeTotalForReceiptsInStore(store: Store)

    /**
     * Recomputes the total for the single receipt given as argument
     *
     * ITS IMPORTANT TO USE THIS METHOD WITH CARE. Remember that products can be represented
     * in MULTIPLE receipts and it is therefore **NOT** advised to call this method when updating
     * products in any way. Instead use [recomputeTotalForReceiptsInStore] for this.
     * This method SHOULD ONLY be used, when a SINGULAR receipt is modified, so f.ex: when
     * changing the total price of a receipt.
     */
    suspend fun recomputeTotalForReceipt(receiptID: Long)

    /**
     * Adds a product to a specific receipt.
     * UI will automatically update via Flow observation.
     */
    suspend fun addProductToReceipt(receiptID: Long, product: Product)

    /**
     * Adds a standalone product (not tied to a receipt).
     * @return The ID of the newly created product
     */
    suspend fun addProduct(product: Product): Long

    /**
     * Updates an existing product.
     * UI will automatically update via Flow observation.
     */
    suspend fun updateProduct(product: Product)

    suspend fun updateReceiptTotal(newTotal: Float, receiptID: Long)

    /**
     * Deletes a product from the database.
     * (Alias for removeProduct - consider removing one)
     */
    suspend fun deleteProduct(product: Product)

    /**
     * Marks a product as favorite/unfavorite by store and name.
     */
    suspend fun setProductFavorite(store: Store, name: String, isFavorite: Boolean)

    /**
     * Observes all products for a specific receipt.
     * Automatically emits when products are added/modified/removed.
     * This is the key method for auto-updating UI!
     */
    fun getProductsForReceipt(receiptID: Long): Flow<List<Product>>

    /**
     * Observes all products from a specific store.
     */
    fun getProductsByStore(store: Store): Flow<List<Product>>

    /**
     * Observes all favorite products.
     */
    fun getFavoriteProducts(): Flow<List<Product>>

    /**
     * Searches for products containing the query string.
     */
    fun searchProductsContaining(query: String): Flow<List<Product>>

    /**
     * Searches for products in a specific store containing the query string.
     */
    fun searchProductsByStoreContaining(store: Store, query: String): Flow<List<Product>>
}