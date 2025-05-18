package weberstudio.app.billigsteprodukter.MVVM

import androidx.lifecycle.ViewModel
import weberstudio.app.billigsteprodukter.Product
import weberstudio.app.billigsteprodukter.Store

class ViewModel: ViewModel() {
    private val model = Model(this)

    ///Adds a product to the store
    fun addProduct(storeName: String, productName: String, productPrice: Float) {
        model.addProduct(storeName, Product(productName, productPrice))
    }
}