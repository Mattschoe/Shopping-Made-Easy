package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.receipt.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.ReceiptWithProducts
import weberstudio.app.billigsteprodukter.logic.Store

class ReceiptScanningViewModel(application: Application): AndroidViewModel(application) {
    private val receiptRepo: OfflineReceiptRepository = (application as ReceiptApp).receiptRepository

    private val _currentReceipt = MutableStateFlow<List<Product>>(emptyList())
    val currentReceipt = _currentReceipt.asStateFlow()

    private val _currentStore = MutableStateFlow<Store?>(null )
    val currentStore = _currentStore.asStateFlow()

    init {
        viewModelScope.launch {
            receiptRepo.lastReceipt.collect { products ->
                if (products.isNotEmpty()) {
                    _currentReceipt.value = products
                    _currentStore.value = products.first().store
                }
            }
        }
    }



    /**
     * Changes the last receipt in THIS viewModel, and not the stored lastReceipt in the repo.
     */
    fun changeLastReceipt(receiptID: Long) {
        viewModelScope.launch {
            val receipt = receiptRepo.getReceiptWithProducts(receiptID)
            receipt?.let { newReceipt ->
                _currentReceipt.value = newReceipt.products
                _currentStore.value = newReceipt.receipt.store
            }
        }
    }


}