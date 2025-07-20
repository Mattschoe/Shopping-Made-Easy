package weberstudio.app.billigsteprodukter.ui.pages.home

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import weberstudio.app.billigsteprodukter.Model
import weberstudio.app.billigsteprodukter.Product

class MainPageViewModel: ViewModel() {
    private val model = Model(this)

    ///Adds a product to the store
    private fun addProduct(storeName: String, productName: String, productPrice: Float) {
        model.addProduct(storeName, Product(productName, productPrice))
    }

    public fun processImage(image: Bitmap) {
        println("Processing Image..")
        model.readImage(InputImage.fromBitmap(image, 0))
    }
}