package weberstudio.app.billigsteprodukter.MVVM

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import weberstudio.app.billigsteprodukter.Product

class ViewModel: ViewModel() {
    private val model = Model(this)

    ///Adds a product to the store
    fun addProduct(storeName: String, productName: String, productPrice: Float) {
        model.addProduct(storeName, Product(productName, productPrice))
    }

    fun processImage(image: Bitmap) {
        println("Processing Image..")
        model.readImage(InputImage.fromBitmap(image, 0))
    }
}