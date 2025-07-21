package weberstudio.app.billigsteprodukter.logic

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import weberstudio.app.billigsteprodukter.Model

class CameraViewModel: ViewModel() {
    private val model = Model

    ///Adds a product to the store
    private fun addProduct(storeName: String, productName: String, productPrice: Float) {
        model.addProduct(storeName, Product(productName, productPrice))
    }

    fun processImage(image: Bitmap) {
        println("Processing Image..")
        model.readImage(InputImage.fromBitmap(image, 0))
    }
}