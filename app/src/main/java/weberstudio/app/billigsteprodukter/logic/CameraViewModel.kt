package weberstudio.app.billigsteprodukter.logic

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import weberstudio.app.billigsteprodukter.logic.parsers.ParserFactory
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser
import weberstudio.app.billigsteprodukter.ui.ParsingState


class CameraViewModel: ViewModel() {
    private val receiptRepo: OfflineReceiptRepository = OfflineReceiptRepository
    private val parsingState = mutableStateOf<ParsingState>(ParsingState.NotActivated)

    /**
     * @param imageURI the URI of the image that needs processing
     * @param context
     */
    suspend fun processImage(imageURI: Uri, context: Context) {
        println("Processing Image..")
        parsingState.value = ParsingState.InProgress
        //Tries to read image
        try {
            val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = ImagePreprocessor.preprocessForMlKit(context, imageURI)

            //Tries to process image
            try {
                val imageText = textRecognizer.process(image).await() //Venter på at vi har læst billedet før vi proceeder

                //Success
                println("Result Success!")
                processImageInfo(imageText)
                textRecognizer.close()
            } catch (e: Exception) {
                //Failure
                ParsingState.Error("MLKit Text recognition failed! $e")
                textRecognizer.close()
            }
        } catch(e: Exception) {
            parsingState.value = ParsingState.Error("Error loading image!: ${e.message}")
        }
        println("Result after!")
        Log.d("STATUS", "Finished parsing with parsing-state: ${parsingState.value}")
    }

    /**
     * Processes the image info
     */
    private fun processImageInfo(imageText: Text) {
        if (imageText.textBlocks.isEmpty()) {
            parsingState.value = ParsingState.Error("Ingen tekst fundet i billedet!")
            return
        }
        runCatching {
            val parser: StoreParser? = ParserFactory.parseReceipt(imageText)
            if (parser != null) {
                println("Adding products to $parser")
                val store: Store? = Store.fromName(parser.toString())
                if (store == null) throw ParsingException("Couldn't find store from ${parser.toString()}!")
                try {
                    val parsedText = parser.parse(imageText)
                    viewModelScope.launch { receiptRepo.addReceiptProducts(parsedText) } //Saves to repository
                    parsingState.value = ParsingState.Success(store)
                } catch (e: ParsingException) {
                    parsingState.value = ParsingState.Error("Fejl med at scanne kvittering, $e")  //TODO: Denne her Error dukker ikke op på UI
                    return@runCatching
                }
            } else {
                parsingState.value = ParsingState.Error("Ingen butik fundet!")
            }
        }
    }

    /**
     * Returns the state of image parsing
     */
    fun getParserState(): State<ParsingState> = parsingState

    /**
     * Adds a singular product to the repo and current receipt
     */
    fun addProductToCurrentReceipt(productName: String, productPrice: Float, store: Store) {
        val product = Product(productName, productPrice, store)
        viewModelScope.launch {
            receiptRepo.addProductToReceipt(product)
        }
    }

    /**
     * Clears the state of the parser, preparing it for next scan
     */
    fun clearParserState() {
        parsingState.value = ParsingState.Idle
    }
}