package weberstudio.app.billigsteprodukter.logic

/**
 * A product in a store.
 */
data class Product(
    val store: Store,
    val name: String,
    val price: Float) {
    val ID: ProductID = ProductID(store, name)
}

/**
 * Unique identifier for "one product in one store"
 */
data class ProductID(
    val store: Store,
    val name: String
)