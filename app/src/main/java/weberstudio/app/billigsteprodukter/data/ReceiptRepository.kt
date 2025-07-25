package weberstudio.app.billigsteprodukter.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.logic.ProductID

object ReceiptRepository {
    private val ID2Product = mutableMapOf<ProductID, Product>()
    private val _products = MutableStateFlow<List<Product>>(emptyList()) //Empty list ensurer at UI'en ikke venter på en update, men ser en tom liste fra start

    /**
     * Stream of the current list of products across receipts and stores.
     */
    val productStream: StateFlow<List<Product>> = _products

    /**
     * Adds the products from a parsed receipt into the list of products. Also dedupes the products via product.id
     */
    suspend fun addReceiptProducts(products: Set<Product>) {
        products.forEach { ID2Product[it.ID] = it } //Overrider produktet med den nye hvis der allerede er et med samme ID
        _products.emit(ID2Product.values.toList()) //Opdaterer og pusher produktlisten downstream
    }

    /**
     * Updates one product, useful if user has corrected a products price or name
     */
    suspend fun updateProduct(product: Product) {
        ID2Product[product.ID] = product
        _products.emit(ID2Product.values.toList()) //Opdater og pusher produktlisten downstream
    }

    /**
     * Remove one product from list
     */
    suspend fun removeProduct(product: Product) {
        ID2Product.remove(product.ID)
        _products.emit(ID2Product.values.toList()) //Opdater og pusher produktlisten downstream
    }
}