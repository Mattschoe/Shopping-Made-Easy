package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.receipt.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.data.Product

class ReceiptScanningViewModel(application: Application): AndroidViewModel(application) {
    private val receiptRepo: OfflineReceiptRepository = (application as ReceiptApp).receiptRepository
    val lastReceipt: StateFlow<List<Product>> = receiptRepo.lastReceipt
}