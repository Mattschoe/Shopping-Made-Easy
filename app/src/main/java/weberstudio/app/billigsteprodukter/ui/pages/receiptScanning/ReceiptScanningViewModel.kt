package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.receipt.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.ui.ReceiptUIState

class ReceiptScanningViewModel(application: Application): AndroidViewModel(application) {
    private val receiptRepo: OfflineReceiptRepository = (application as ReceiptApp).receiptRepository

    private val _uiState = MutableStateFlow<ReceiptUIState>(ReceiptUIState.Empty)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            receiptRepo.lastReceipt.collect { products ->
                if (products.isNotEmpty()) {
                    _uiState.value = ReceiptUIState.Success(
                        products = products,
                        store = products.first().store
                    )
                }
            }
        }
    }

    fun loadReceipt(receiptID: Long) {
        viewModelScope.launch {
            val receipt = receiptRepo.getReceiptWithProducts(receiptID)
            receipt?.let { receiptWithProducts ->
                _uiState.value = ReceiptUIState.Success(
                    products = receiptWithProducts.products,
                    store = receiptWithProducts.receipt.store
                )
            } ?: run {
                _uiState.value = ReceiptUIState.Empty
            }
        }
    }

    /**
     * Shows loading state for new receipt scans
     */
    fun showLoadingState() {
        _uiState.value = ReceiptUIState.Loading
    }

    fun clearReceipt() {
        _uiState.value = ReceiptUIState.Empty
    }
}