package weberstudio.app.billigsteprodukter.MVVM

import weberstudio.app.billigsteprodukter.Product
import weberstudio.app.billigsteprodukter.Store

class Model(val viewModel: ViewModel) {
    private val stores = HashMap<String, Store>()

    ///Adds the given product to the store. If no store creates a new one
    fun addProduct(storeName: String, product: Product) {
        val store = stores.getOrPut(storeName) { Store(storeName) }
        store.products.add(product)
    }
}