package weberstudio.app.billigsteprodukter.data.receipt

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.Receipt
import weberstudio.app.billigsteprodukter.data.ReceiptWithProducts
import weberstudio.app.billigsteprodukter.logic.Store
import java.time.Month
import java.time.Year
import java.time.YearMonth

class OfflineReceiptRepository(private val dao: ReceiptDao) : ReceiptRepository {

    override suspend fun addReceiptProducts(receipt: Receipt, products: Set<Product>): Long {
        val productsList = products.toList()
        return dao.insertReceiptWithProducts(receipt, productsList)
    }

    override suspend fun deleteReceipt(receipt: Receipt) {
        dao.deleteReceipt(receipt)
    }

    override suspend fun getReceiptWithProducts(receiptID: Long): ReceiptWithProducts? {
        return dao.getReceiptWithProducts(receiptID)
    }

    override suspend fun getReceiptsForMonth(month: Month, year: Year): Flow<List<ReceiptWithProducts>> {
        val startDate = YearMonth.of(year.value, month).atDay(1).toEpochDay()
        val endDate = YearMonth.of(year.value, month).atEndOfMonth().toEpochDay()
        return dao.getReceiptsBetweenDates(startDate, endDate)
    }

    override suspend fun recomputeTotalForReceiptsInStore(store: Store) {
        return dao.recomputeTotalForReceiptsInStore(store)
    }

    /**
     * Adds a product to a specific receipt.
     * The UI will automatically update via Room's Flow observation.
     */
    override suspend fun addProductToReceipt(receiptID: Long, product: Product) {
        val productWithReceiptID = product.copy(receiptID = receiptID)
        dao.insertProducts(listOf(productWithReceiptID))
        recomputeTotalForReceiptsInStore(product.store)
    }

    override suspend fun updateProduct(product: Product) {
        dao.updateProduct(product)
        recomputeTotalForReceiptsInStore(product.store)
    }

    override suspend fun addProduct(product: Product): Long {
        return dao.insertProduct(product)
    }

    override suspend fun deleteProduct(product: Product) {
        dao.deleteProduct(product)
        recomputeTotalForReceiptsInStore(product.store)
    }

    override suspend fun setProductFavorite(store: Store, name: String, isFavorite: Boolean) {
        return dao.setProductFavorite(store, name, isFavorite)
    }

    /**
     * Observes all products for a specific receipt.
     * Automatically emits new values when products are added/modified/removed.
     * This is the key method that makes the UI auto-update!
     */
    override fun getProductsForReceipt(receiptID: Long): Flow<List<Product>> {
        return dao.getProductsForReceipt(receiptID)
    }

    override fun getProductsByStore(store: Store): Flow<List<Product>> {
        return dao.getProductsByStore(store)
    }

    override fun getFavoriteProducts(): Flow<List<Product>> {
        return dao.getFavoriteProducts()
    }

    override fun searchProductsContaining(query: String): Flow<List<Product>> {
        return dao.searchProductsContaining(query)
    }

    override fun searchProductsByStoreContaining(store: Store, query: String): Flow<List<Product>> {
        return dao.searchProductsByStoreContaining(store, query)
    }
}