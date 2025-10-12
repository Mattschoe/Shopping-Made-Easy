package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.Receipt
import weberstudio.app.billigsteprodukter.data.receipt.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.logic.ImagePreprocessor
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import weberstudio.app.billigsteprodukter.logic.parsers.ParserFactory
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser
import weberstudio.app.billigsteprodukter.ui.ParsingState
import weberstudio.app.billigsteprodukter.ui.ReceiptUIState
import java.time.LocalDate

class ReceiptViewModel(application: Application): AndroidViewModel(application) {
    private val app = application as ReceiptApp
    private val receiptRepo: OfflineReceiptRepository = app.receiptRepository

    // Receipt display state
    private val _selectedReceiptID = MutableStateFlow<Long?>(null)
    private val _forceLoading = MutableStateFlow(false)

    // Camera/parsing state
    private val _parsingState = mutableStateOf<ParsingState>(ParsingState.NotActivated)
    val parsingState: State<ParsingState> = _parsingState

    /**
     * The currently displayed receipt with all its products.
     * Automatically updates when products are added/modified/removed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ReceiptUIState> = combine(
        _selectedReceiptID,
        _forceLoading
    ) { receiptID, forceLoading ->
        Pair(receiptID, forceLoading)
    }.flatMapLatest { (receiptID, forceLoading) ->
        when {
            forceLoading -> flowOf(ReceiptUIState.Loading)
            receiptID == null -> flowOf(ReceiptUIState.Empty)
            else -> receiptRepo.getProductsForReceipt(receiptID).map { products ->
                if (products.isNotEmpty()) {
                    ReceiptUIState.Success(
                        products = products,
                        store = products.first().store,
                        receiptTotal = receiptRepo.getReceiptWithProducts(receiptID)?.receipt?.total ?: 0.0f
                    )
                } else {
                    ReceiptUIState.Empty
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ReceiptUIState.Empty
    )


    fun loadReceipt(receiptID: Long) {
        _forceLoading.value = false
        _selectedReceiptID.value = receiptID
    }

    /**
     * Shows loading state (typically when waiting for camera scan to complete).
     */
    fun showLoadingState() {
        _forceLoading.value = true
    }

    /**
     * Updates a product in the database.
     * The UI will automatically update via the Flow.
     */
    fun updateProduct(product: Product) {
        viewModelScope.launch {
            receiptRepo.updateProduct(product)
        }
    }

    /**
     * Adds a product to the currently displayed receipt.
     */
    fun addProductToCurrentReceipt(productName: String, productPrice: Float, store: Store) {
        val receiptID = _selectedReceiptID.value ?: return
        val product = Product(name = productName, price = productPrice, store = store)

        viewModelScope.launch {
            receiptRepo.addProductToReceipt(receiptID, product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            receiptRepo.deleteProduct(product)
        }
    }

    //region CAMERA SCANNING METHODS
    /**
     * Processes an image captured from the camera.
     * Will parse the receipt and save it to the database.
     */
    fun processImage(imageURI: Uri, context: Context) {
        viewModelScope.launch {
            _parsingState.value = ParsingState.InProgress

            try {
                val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val image = ImagePreprocessor.preprocessForMlKit(context, imageURI)

                try {
                    val imageText = textRecognizer.process(image).await()
                    processImageInfo(imageText)
                    textRecognizer.close()
                } catch (e: Exception) {
                    _parsingState.value = ParsingState.Error("MLKit Text recognition failed! $e")
                    textRecognizer.close()
                }
            } catch(e: Exception) {
                _parsingState.value = ParsingState.Error("Error loading image!: ${e.message}")
            }
            Log.d("ReceiptViewModel", "Finished parsing with state: ${_parsingState.value}")
        }
    }

    private fun processImageInfo(imageText: Text) {
        if (imageText.textBlocks.isEmpty()) {
            _parsingState.value = ParsingState.Error("Ingen tekst fundet i billedet!")
            return
        }

        runCatching {
            val parser: StoreParser? = ParserFactory.parseReceipt(imageText)
            if (parser != null) {
                val store: Store? = Store.fromName(parser.toString())
                if (store == null) {
                    throw ParsingException("Couldn't find store from ${parser}!")
                }

                try {
                    val parsedText = parser.parse(imageText)

                    val receipt = Receipt(
                        store = store,
                        date = LocalDate.now(),
                        total = parsedText.total
                    )

                    viewModelScope.launch {
                        val receiptID = receiptRepo.addReceiptProducts(receipt, parsedText.products)
                        app.activityLogger.logReceiptScan(receipt.copy(receiptID = receiptID))
                        _parsingState.value = ParsingState.Success(store, receiptID)
                    }
                } catch (e: ParsingException) {
                    _parsingState.value = ParsingState.Error("Fejl med at scanne kvittering, $e")
                }
            } else {
                _parsingState.value = ParsingState.Error("Ingen butik fundet!")
            }
        }
    }

    /**
     * Clears the parsing state, preparing for the next scan.
     */
    fun clearParsingState() {
        _parsingState.value = ParsingState.Idle
    }
    //endregion
}
