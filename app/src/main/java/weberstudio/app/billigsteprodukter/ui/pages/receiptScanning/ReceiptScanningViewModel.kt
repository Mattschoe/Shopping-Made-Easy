package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.data.ReceiptRepository
import weberstudio.app.billigsteprodukter.data.Product

class ReceiptScanningViewModel: ViewModel() {
    private val receiptRepo: ReceiptRepository = ReceiptRepository
    val lastReceipt: StateFlow<List<Product>> = receiptRepo.lastReceipt

    /**
     * Adds the products from a parsed receipt into the list of products. This also updates the latest receipt
     */
    fun saveReceiptToRepository(products: Set<Product>) {
        viewModelScope.launch {
            receiptRepo.addReceiptProducts(products)
        }
    }
}