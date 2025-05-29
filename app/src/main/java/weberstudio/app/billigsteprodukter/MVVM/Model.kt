package weberstudio.app.billigsteprodukter.MVVM

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import weberstudio.app.billigsteprodukter.Product
import weberstudio.app.billigsteprodukter.Store

class Model(val viewModel: ViewModel) {
    private val stores = HashMap<String, Store>()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    ///Adds the given product to the store. If no store creates a new one
    public fun addProduct(storeName: String, product: Product) {
        val store = stores.getOrPut(storeName) { Store(storeName) }
        store.products.add(product)
    }

    ///Reads the given image and tries to process the info
    public fun readImage(image: InputImage) {
        val result = textRecognizer.process(image)
            .addOnSuccessListener { imageText -> processImageInfo(imageText) }
            .addOnFailureListener { _ -> println("Failed reading image!") }
    }

    ///Processes the image info
    private fun processImageInfo(imageText: Text) {
        if (imageText.textBlocks.isEmpty()) println("No text found in image!")
        for (block in imageText.textBlocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    println(element.text)
                }
            }
        }
    }

}