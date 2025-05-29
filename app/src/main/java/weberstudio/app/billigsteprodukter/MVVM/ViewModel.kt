package weberstudio.app.billigsteprodukter.MVVM

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import weberstudio.app.billigsteprodukter.Product

class ViewModel: ViewModel() {
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