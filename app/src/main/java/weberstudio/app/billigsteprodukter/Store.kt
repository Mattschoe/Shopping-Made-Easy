package weberstudio.app.billigsteprodukter

import androidx.annotation.IntegerRes

data class Store(val name: String) {
    val products = HashSet<Product>()
}