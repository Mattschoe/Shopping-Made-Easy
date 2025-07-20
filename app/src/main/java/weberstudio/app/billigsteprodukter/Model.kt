package weberstudio.app.billigsteprodukter

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import weberstudio.app.billigsteprodukter.parsers.ParserFactory
import weberstudio.app.billigsteprodukter.parsers.StoreParser
import weberstudio.app.billigsteprodukter.ui.pages.home.MainPageViewModel

class Model(val viewModel: MainPageViewModel) {
    private val stores = HashMap<String, Store>()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    ///Reads the given image and tries to process the info
    public fun readImage(image: InputImage) {
        val result = textRecognizer.process(image)
            .addOnSuccessListener { imageText -> processImageInfo(imageText) }
            .addOnFailureListener { _ -> println("Failed reading image!") }
    }

    ///Processes the image info
    private fun processImageInfo(imageText: Text) {
        if (imageText.textBlocks.isEmpty()) {
            println("No text found in image!")
            return
        }
        val parser: StoreParser? = ParserFactory.parseReceipt(imageText)
        if (parser != null) {
            println("Adding products to ${parser.toString()}")
            addProducts(parser.toString(), parser.parse(imageText))
        }
        else println("No store found!")
    }

    ///Adds the given product to the store. If no store creates a new one
    private fun addProducts(storeName: String, products: HashSet<Product>) {
        val store = stores.getOrPut(storeName) { Store(storeName) }
        store.products.addAll(products)
    }

    ///Adds the given product to the store. If no store creates a new one
    public fun addProduct(storeName: String, product: Product) {
        val store = stores.getOrPut(storeName) { Store(storeName) }
        store.products.add(product)
    }
}