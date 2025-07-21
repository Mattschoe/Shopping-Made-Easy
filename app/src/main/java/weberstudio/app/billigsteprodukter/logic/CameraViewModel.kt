package weberstudio.app.billigsteprodukter.logic

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import weberstudio.app.billigsteprodukter.Model
import weberstudio.app.billigsteprodukter.logic.parsers.ParserFactory
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser
import java.io.File

class CameraViewModel: ViewModel() {
    private val model = Model

    ///Adds a product to the store
    private fun addProduct(storeName: String, productName: String, productPrice: Float) {
        model.addProduct(storeName, Product(productName, productPrice))
    }

    /**
     * @param imageURI the URI of the image that needs processing
     * @param context
     */
    fun processImage(imageURI: Uri, context: Context) {
        println("Processing Image..")
        //Tries to read image
        try {
            val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromFilePath(context, imageURI)

            //Tries to process image
            textRecognizer.process(image)
                .addOnSuccessListener { imageText ->
                    processImageInfo(imageText)
                    textRecognizer.close()
                }
                .addOnFailureListener { e ->
                    println("MLKit Text recognition failed! $e")
                    textRecognizer.close()
                }
        } catch(e: Exception) {
            print("Error loading image!: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Processes the image info
     */
    private fun processImageInfo(imageText: Text) {
        if (imageText.textBlocks.isEmpty()) {
            println("No text found in image!")
            return
        }
        val parser: StoreParser? = ParserFactory.parseReceipt(imageText)
        if (parser != null) {
            println("Adding products to ${parser.toString()}")
            model.addProducts(parser.toString(), parser.parse(imageText))
        }
        else println("No store found!")
    }
}