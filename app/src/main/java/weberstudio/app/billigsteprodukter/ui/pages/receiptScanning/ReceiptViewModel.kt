package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.apache.commons.lang3.NotImplementedException
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.Receipt
import weberstudio.app.billigsteprodukter.data.receipt.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.data.settings.Coop365Option
import weberstudio.app.billigsteprodukter.data.settings.SettingsRepository
import weberstudio.app.billigsteprodukter.data.settings.TotalOption
import weberstudio.app.billigsteprodukter.data.settings.TotalOption.*
import weberstudio.app.billigsteprodukter.logic.CameraCoordinator
import weberstudio.app.billigsteprodukter.logic.ImagePreprocessor
import weberstudio.app.billigsteprodukter.logic.Logger
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import weberstudio.app.billigsteprodukter.logic.parsers.ParserFactory
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ScanValidation
import weberstudio.app.billigsteprodukter.ui.ParsingState
import weberstudio.app.billigsteprodukter.ui.ReceiptUIState
import java.time.LocalDate

class ReceiptViewModel(application: Application): AndroidViewModel(application) {
    private val tag = "ReceiptViewModel"
    private val app = application as ReceiptApp
    private val receiptRepo: OfflineReceiptRepository = app.receiptRepository
    private val settingsRepo: SettingsRepository = app.settingsRepository

    //Receipt display state
    private val _selectedReceiptID = MutableStateFlow<Long?>(null)
    private val _forceLoading = MutableStateFlow(false)
    private val _totalOption = settingsRepo.totalOption.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PRODUCT_TOTAL
    )

    //Camera/parsing state
    private val _parsingState = MutableStateFlow<ParsingState>(ParsingState.NotActivated)
    val parsingState = _parsingState.asStateFlow()

    //Errors
    private val _errors = MutableStateFlow<ScanValidation?>(null)

    /**
     * The currently displayed receipt with all its products.
     * Automatically updates when products are added/modified/removed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ReceiptUIState> = combine(
        _selectedReceiptID,
        _forceLoading,
        _errors,
        _totalOption
    ) { receiptID, forceLoading, errors, totalOption ->
        data class CombinedState(val receiptID: Long?, val forceLoading: Boolean, val errors: ScanValidation?, val totalOption: TotalOption)
        CombinedState(receiptID, forceLoading, errors, totalOption)
    }.flatMapLatest { (receiptID, forceLoading, errors, totalOption) ->
        when {
            forceLoading -> flowOf(ReceiptUIState.Loading)
            receiptID == null -> flowOf(ReceiptUIState.Empty)
            else -> receiptRepo.getReceiptWithProducts(receiptID).map { receiptWithProducts ->
                if (receiptWithProducts?.products?.isNotEmpty() == true) {
                    val total = when (totalOption) {
                        RECEIPT_TOTAL -> receiptWithProducts.receipt.total
                        PRODUCT_TOTAL -> receiptWithProducts.products.sumOf { it.price.toDouble() }.toFloat()
                    }

                    ReceiptUIState.Success(
                        products = receiptWithProducts.products,
                        store = receiptWithProducts.receipt.store,
                        receiptTotal = total,
                        errors = errors
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
     * Updates the total for a receipt. A receipt HAS to be selected for this to run
     */
    fun updateTotalForSelectedReceipt(newTotal: Float) {
        _selectedReceiptID.value?.let { ID ->
            viewModelScope.launch {
                receiptRepo.updateReceiptTotal(newTotal, ID)
            }
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
    fun processImage(imageURI: Uri, context: Context, cameraCoordinator: CameraCoordinator) {
        Logger.log(tag, "Processing image")
        viewModelScope.launch {
            _parsingState.value = ParsingState.InProgress
            val coop365Option = settingsRepo.coop365Option.firstOrNull()

            if (coop365Option == null) {
                Logger.log(tag, "Coop365Option not sat")
                ParsingState.Error("Coop365Option ikke valgt! Se indstillinger.")
                return@launch
            }

            try {
                val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val image = ImagePreprocessor.preprocessForMlKit(context, imageURI)

                try {
                    val imageText = textRecognizer.process(image).await()
                    Logger.log(tag, "ImageText processed")
                    processImageInfo(imageText, cameraCoordinator, coop365Option)
                    Logger.log(tag, "ImageInfo processed")
                    textRecognizer.close()
                } catch (e: Exception) {
                    _parsingState.value = ParsingState.Error("Prøv venligst igen.")
                    Logger.log(tag, "Parsing error: ${e.toString()}")
                    textRecognizer.close()
                }
            } catch(e: Exception) {
                _parsingState.value = ParsingState.Error("Kunne ikke loade billedet. Prøv venligst igen.")
                Logger.log(tag, "Parsing error: ${e.message.toString()}")
            }
        }
        Logger.log(tag, "Image processing done")
    }

    private fun processImageInfo(
        imageText: Text, cameraCoordinator: CameraCoordinator,
        coop365Option: Coop365Option.Option
    ) {
        if (imageText.textBlocks.isEmpty()) {
            Logger.log(tag, "No image found in picture")
            _parsingState.value = ParsingState.Error("Ingen tekst fundet i billedet!")
            return
        }

        runCatching {
            val parser = try {
                ParserFactory.parseReceipt(imageText, coop365Option)
            } catch (e: NotImplementedException) {
                Logger.log(tag, "Parsing Exception: ${e.toString()}")
                throw ParsingException(e.toString())
            }
            Logger.log(tag, "Parser: $parser")
            if (parser != null) {
                val store: Store? = Store.fromName(parser.toString())
                if (store == null) {
                    Logger.log(tag, "Ingen butik fundet")
                    throw ParsingException("Prøv venligst igen.")
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
                        Logger.log(tag, "Finished with receipt with data: $receipt and ID: $receiptID")
                        cameraCoordinator.setScanValidation(receiptID, parsedText.scanErrors)
                        app.activityLogger.logReceiptScan(receipt.copy(receiptID = receiptID))
                        _parsingState.value = ParsingState.Success(store, receiptID)
                    }
                } catch (e: ParsingException) {
                    Logger.log(tag, "Error in scanning receipt: $e")
                    _parsingState.value = ParsingState.Error("Fejl med at scanne kvittering, $e")
                }
            } else {
                Logger.log(tag, "No store found")
                _parsingState.value = ParsingState.Error("Ingen butik fundet! Prøv venligst at inkludere billedet af butikslogoet")
            }
        }
    }

    fun applyScanValidation(validation: ScanValidation) {
        Logger.log(tag, "Applying scan validation, with validation: $validation")
        _errors.value = validation
    }

    /**
     * Clears the parsing state, preparing for the next scan.
     */
    fun clearParsingState() {
        Logger.log(tag, "Clearing parsing state")
        _parsingState.value = ParsingState.Idle
    }
    //endregion
}
