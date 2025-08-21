package weberstudio.app.billigsteprodukter.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import weberstudio.app.billigsteprodukter.logic.Store

class OfflineReceiptRepository(private val dao: ReceiptDao) : ReceiptRepository {
    private val _lastReceipt = MutableStateFlow<List<Product>>(emptyList())
    override val lastReceipt: StateFlow<List<Product>> = _lastReceipt

    override suspend fun addReceiptProducts(store: Store, products: Set<Product>, receiptTotal: Float) {
        //Creates receipt
        val receipt = Receipt(
            store = store,
            date = System.currentTimeMillis(),
            total = receiptTotal
        )

        val productsList = products.toList()

        dao.insertReceiptWithProducts(receipt, productsList)
        updateStreams(productsList)
    }

    override fun getReceiptsBetweenDates(startDate: Long, endDate: Long): Flow<List<ReceiptWithProducts>> {
        return dao.getReceiptsBetweenDates(startDate, endDate)
    }

    override suspend fun getReceiptWithProducts(receiptID: Long): ReceiptWithProducts? {
        return dao.getReceiptWithProducts(receiptID)
    }

    override suspend fun updateProduct(product: Product) {
        dao.updateProduct(product)
    }

    override suspend fun addProductToCurrentReceipt(product: Product): Boolean {
        val currentLastReceipt = _lastReceipt.value

        if (currentLastReceipt.isEmpty()) return false
        if (product.store != currentLastReceipt.first().store) return false

        //Adds to current receipt
        val lastReceiptID = currentLastReceipt.first().receiptID
        val productWithReceiptID = product.copy(receiptID = lastReceiptID) //Makes sure the product added has the reference to the receipt it's being added to

        dao.insertProducts(listOf(productWithReceiptID))
        updateStreams(currentLastReceipt + productWithReceiptID)
        return true
    }

    override suspend fun removeProduct(product: Product) {
        dao.deleteProduct(product)

        //If the removed product is part of the receipt we remove it from the lastReceipt
        val updatedLastReceipt = _lastReceipt.value.filter {
            it.businessID != product.businessID
        }
        updateStreams(updatedLastReceipt)
    }

    override fun getProductsByStore(store: Store): Flow<List<Product>> {
        return dao.getProductsByStore(store)
    }

    override suspend fun setProductFavorite(store: Store, name: String, isFavorite: Boolean) {
        return dao.setProductFavorite(store, name, isFavorite)
    }

    override fun getFavoriteProducts(): Flow<List<Product>> {
        return dao.getFavoriteProducts()
    }

    /**
     * Updates and refreshes the streams so the UI is informed of changes. Method should be called on any Insertion/Update/Deletion of [_lastReceipt]
     */
    private suspend fun updateStreams(productsList: List<Product>) {
        _lastReceipt.emit(productsList)
    }
}