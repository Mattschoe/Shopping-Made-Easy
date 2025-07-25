package weberstudio.app.billigsteprodukter.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.data.ReceiptRepository
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import weberstudio.app.billigsteprodukter.logic.parsers.ParserFactory
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser
import weberstudio.app.billigsteprodukter.ui.ParsingState
import java.io.File


class CameraViewModel: ViewModel() {
    private val receiptRepo: ReceiptRepository = ReceiptRepository
    private val parsingState = mutableStateOf<ParsingState>(ParsingState.NotActivated)

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
            //OBS: Jeg har ingen fucking ide, men hvis vi ikke loader det som et bitmap og så inputImage så er det kun sådan 50/50 om den faktisk går ned i en listener, omfg
            val imageFile = File(context.cacheDir, "tempImage.jpg")
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            val image = InputImage.fromBitmap(bitmap, 0)

            //Tries to process image
            textRecognizer.process(image)
                .addOnSuccessListener { imageText ->
                    parsingState.value = ParsingState.Error("Test error")
                    processImageInfo(imageText)
                    textRecognizer.close()
                }
                .addOnFailureListener { e ->
                    parsingState.value = ParsingState.Error("MLKit Text recognition failed! $e")
                    textRecognizer.close()
                }
        } catch(e: Exception) {
            parsingState.value = ParsingState.Error("Error loading image!: ${e.message}")
        }
        println("Finished parsing with parsing-state: ${parsingState.value}")
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
                try {
                    val parsedText = parser.parse(imageText)
                    viewModelScope.launch { receiptRepo.addReceiptProducts(parsedText) } //Saves to repository
                    parsingState.value = ParsingState.Success
                } catch (e: ParsingException) {
                    parsingState.value = ParsingState.Error("Fejl med at scanne kvittering, $e")
                }
            }
            parsingState.value = ParsingState.Error("Ingen butik fundet!")
        }
    }

    /**
     * Returns the state of image parsing
     */
    fun getParserState(): State<ParsingState> = parsingState

    /**
     * Clears the state of the parser, preparing it for next scan
     */
    fun clearParserState() {
        parsingState.value = ParsingState.Idle
    }
}