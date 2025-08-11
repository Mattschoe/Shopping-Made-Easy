package weberstudio.app.billigsteprodukter.logic

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A product in a store.
 */
data class Product(
    val name: String,
    val price: Float,
    val store: Store,
    var isFavorite: Boolean = false
    ) {
    val ID: ProductID = ProductID(store, name)
}

/**
 * Unique identifier for "one product in one store"
 */
@Parcelize
data class ProductID(
    val store: Store,
    val name: String
): Parcelable