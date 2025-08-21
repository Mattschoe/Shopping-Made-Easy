package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.data.Product

class ReceiptScanningViewModel(application: Application): AndroidViewModel(application) {
    private val receiptRepo: OfflineReceiptRepository = (application as ReceiptApp).receiptRepository
    val lastReceipt: StateFlow<List<Product>> = receiptRepo.lastReceipt
}