package weberstudio.app.billigsteprodukter.logic

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import weberstudio.app.billigsteprodukter.Model
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import weberstudio.app.billigsteprodukter.logic.parsers.ParserFactory
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser
import weberstudio.app.billigsteprodukter.ui.ParsingState

class CameraViewModel: ViewModel() {
    private val model = Model
    private val parsingState = mutableStateOf<ParsingState>(ParsingState.Idle)

    /**
     * @param imageURI the URI of the image that needs processing
     * @param context
     */
    fun processImage(imageURI: Uri, context: Context) {
        println("Processing Image..")
        parsingState.value = ParsingState.InProgress
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
            parsingState.value = ParsingState.Error("Error loading image!: ${e.message}")
        }
    }

    /**
     * Processes the image info
     */
    private fun processImageInfo(imageText: Text) {
        if (imageText.textBlocks.isEmpty()) {
            parsingState.value = ParsingState.Error("Ingen tekst fundet i billedet!")
        }
        val parser: StoreParser? = ParserFactory.parseReceipt(imageText)
        if (parser != null) {
            println("Adding products to $parser")
            try {
                model.addProducts(parser.toString(), parser.parse(imageText))
                ParsingState.Success
            } catch (e: ParsingException) {
                ParsingState.Error("Fejl med at scanne kvittering, $e")
            }
        }
        parsingState.value = ParsingState.Error("Ingen butik fundet!")
    }

    /**
     * Returns the state of image parsing
     */
    fun getParserState(): State<ParsingState> = parsingState
}