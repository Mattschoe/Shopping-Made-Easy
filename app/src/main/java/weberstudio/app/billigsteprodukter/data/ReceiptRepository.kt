package weberstudio.app.billigsteprodukter.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.logic.ProductID
import weberstudio.app.billigsteprodukter.logic.Store

object ReceiptRepository {
    private val ID2Product = mutableMapOf<ProductID, Product>()
    private val _products = MutableStateFlow<List<Product>>(emptyList()) //Empty list ensurer at UI'en ikke venter på en update, men ser en tom liste fra start
    private val _lastReceipt = MutableStateFlow<List<Product>>(emptyList())
    /**
     * Stream of the current list of products across receipts and stores.
     */
    val productStream: StateFlow<List<Product>> = _products
    /**
     * Stream of the last receipt received
     */
    val lastReceipt: StateFlow<List<Product>> = _lastReceipt


    /**
     * Adds the products from a parsed receipt into the list of products. Also dedupes the products via product.id
     */
    suspend fun addReceiptProducts(products: Set<Product>) {
        products.forEach { ID2Product[it.ID] = it } //Overrider produktet med den nye hvis der allerede er et med samme ID
        refreshStream()
        _lastReceipt.emit(products.toList()) //Opdaterer den sidste modtaget kvittering
    }

    /**
     * Updates one product, useful if user has corrected a products price or name
     */
    suspend fun updateProduct(product: Product) {
        ID2Product[product.ID] = product
        refreshStream()
    }

    /**
     * Adds a extra product to the repo and to the current receipt if its part of the same store as the lastReceipt
     */
    suspend fun addProductToReceipt(product: Product) {
        val lastReceiptStore = _lastReceipt.value.first().store
        if (product.store == lastReceiptStore) {
            _lastReceipt.update { currentList -> currentList + product }
        }
        updateProduct(product)
        refreshStream()
    }

    /**
     * Remove one product from list
     */
    suspend fun removeProduct(product: Product) {
        ID2Product.remove(product.ID)
        refreshStream()
    }

    /**
     * Refreshes the stream so any listeners (UI) updates
     */
    private suspend fun refreshStream() {
        _products.emit(ID2Product.values.toList()) //Opdater og pusher produktlisten downstream
    }

    /**
     * Returns all the products from the store given as argument
     */
    fun getProductsByStore(store: Store): Flow<List<Product>> =
        productStream
            .map { allProducts ->
                allProducts.filter { it.ID.store == store }
            }
            .distinctUntilChanged()

    suspend fun setProductFavorite(ID: ProductID, updatedProductFavoriteStatus: Boolean) {
        ID2Product[ID]!!.isFavorite = updatedProductFavoriteStatus
        refreshStream()
    }
}