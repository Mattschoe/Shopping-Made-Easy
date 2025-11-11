package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import android.app.Application
import android.content.Context
import android.net.Uri
import weberstudio.app.billigsteprodukter.logic.Formatter.isIshEqualTo
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
import weberstudio.app.billigsteprodukter.logic.Formatter.normalizeText
import weberstudio.app.billigsteprodukter.logic.ImagePreprocessor
import weberstudio.app.billigsteprodukter.logic.Logger
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.components.FuzzyMatcher
import weberstudio.app.billigsteprodukter.logic.exceptions.ParsingException
import weberstudio.app.billigsteprodukter.logic.parsers.ParserFactory
import weberstudio.app.billigsteprodukter.logic.parsers.ParserFactory.detectStore
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

            val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            try {
                Logger.log(tag, "First OCR pass: Full image")
                val fullImage = ImagePreprocessor.preprocessForMlKit(context, imageURI)
                val fullImageText = textRecognizer.process(fullImage).await()

                if (fullImageText.textBlocks.isEmpty()) {
                    Logger.log(tag, "No text found in picture")
                    _parsingState.value = ParsingState.Error("Ingen tekst fundet i billedet!")
                    textRecognizer.close()
                    return@launch
                }

                val detectedStore = detectStore(fullImageText)
                if (detectedStore == null) {
                    Logger.log(tag, "No store found in first pass")
                    _parsingState.value = ParsingState.Error(
                        "Ingen butik fundet! Prøv venligst at inkludere billedet af butikslogoet"
                    )
                    textRecognizer.close()
                    return@launch
                }
                Logger.log(tag, "Store detected: $detectedStore")

                when (val cropResult = ImagePreprocessor.preprocessAndCropForMlKit(
                    context, imageURI, fullImageText, detectedStore
                )) {
                    is ImagePreprocessor.CropResult.Success -> {
                        Logger.log(tag, "Crop successful, running second OCR pass")
                        val croppedImageText = textRecognizer.process(cropResult.inputImage).await()

                        // Parse products from cropped image
                        parseAndSaveReceipt(
                            imageText = croppedImageText,
                            store = detectedStore,
                            cameraCoordinator = cameraCoordinator,
                            coop365Option = coop365Option
                        )
                    }

                    is ImagePreprocessor.CropResult.Failed -> {
                        // Fallback: Use full image if cropping fails
                        Logger.log(tag, "Cropping failed: ${cropResult.reason}. Using full image.")
                        parseAndSaveReceipt(
                            imageText = fullImageText,
                            store = detectedStore,
                            cameraCoordinator = cameraCoordinator,
                            coop365Option = coop365Option
                        )
                    }
                }
                textRecognizer.close()
            } catch(e: Exception) {
                Logger.log(tag, "Error processing image: ${e.message}")
                _parsingState.value = ParsingState.Error("Kunne ikke behandle billedet. Prøv venligst igen.")
                textRecognizer.close()
            }
        }
        Logger.log(tag, "Image processing done")
    }



    /**
     * Parse receipt and save to database
     */
    private fun parseAndSaveReceipt(
        imageText: Text,
        store: Store,
        cameraCoordinator: CameraCoordinator,
        coop365Option: Coop365Option.Option
    ) {
        try {
            val parser = ParserFactory.parseReceipt(imageText, coop365Option)
            if (parser == null) {
                Logger.log(tag, "Parser returned null")
                _parsingState.value = ParsingState.Error("Kunne ikke parse kvittering. Prøv igen.")
                return
            }

            Logger.log(tag, "Parsing with: ${parser.javaClass.simpleName}")

            val parsedText = parser.parse(imageText)
            val receipt = Receipt(
                store = store,
                date = LocalDate.now(),
                total = parsedText.total
            )

            viewModelScope.launch {
                val receiptID = receiptRepo.addReceiptProducts(receipt, parsedText.products)
                Logger.log(tag, "Receipt saved with ID: $receiptID, products: ${parsedText.products.size}")

                //Removes the total error if product total corresponds with what the parser calculated
                val errors = if (_totalOption.value == PRODUCT_TOTAL) {
                    val isValid = parsedText.products
                        .sumOf { it.price.toDouble() }
                        .toFloat()
                        .isIshEqualTo(parsedText.total)
                    if (isValid) parsedText.scanErrors.copy(totalError = false)
                    else parsedText.scanErrors
                } else {
                    parsedText.scanErrors
                }

                cameraCoordinator.setScanValidation(receiptID, errors)
                app.activityLogger.logReceiptScan(receipt.copy(receiptID = receiptID))

                _parsingState.value = ParsingState.Success(store, receiptID)
            }

        } catch (e: ParsingException) {
            Logger.log(tag, "Parsing exception: ${e.message}")
            _parsingState.value = ParsingState.Error("Fejl med at scanne kvittering: ${e.message}")
        } catch (e: Exception) {
            Logger.log(tag, "Unexpected error: ${e.message}")
            _parsingState.value = ParsingState.Error("Uventet fejl: ${e.message}")
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
