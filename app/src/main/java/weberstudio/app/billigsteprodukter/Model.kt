package weberstudio.app.billigsteprodukter

import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.parsers.ParserFactory
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser
import weberstudio.app.billigsteprodukter.logic.CameraViewModel

object Model {
    private val stores = HashMap<String, Store>()

    /**
     * Adds the given product to the store. If no store creates a new one
     */
    fun addProducts(storeName: String, products: HashSet<Product>) {
        val store = stores.getOrPut(storeName) { Store(storeName) }
        store.products.addAll(products)
    }

    /**
     * Adds the given product to the store. If no store creates a new one
     */
    fun addProduct(storeName: String, product: Product) {
        val store = stores.getOrPut(storeName) { Store(storeName) }
        store.products.add(product)
    }
}