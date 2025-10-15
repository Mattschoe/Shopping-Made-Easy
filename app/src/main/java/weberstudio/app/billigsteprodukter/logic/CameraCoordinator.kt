package weberstudio.app.billigsteprodukter.logic

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ScanValidation

/*
* This ViewModel does NOT contain business logic - it's just a temporary data holder.
*/
class CameraCoordinator : ViewModel() {
    private val _pendingImageCapture = MutableStateFlow<PendingCapture?>(null)
    val pendingImageCapture: StateFlow<PendingCapture?> = _pendingImageCapture.asStateFlow()

    private val _pendingScanValidation = MutableStateFlow<PendingScanValidation?>(null)
    val pendingScanValidation: StateFlow<PendingScanValidation?> = _pendingScanValidation

    /**
     * Called when an image is captured from the camera.
     * Stores it temporarily until the receipt screen can process it.
     */
    fun onImageCaptured(uri: Uri, context: Context) {
        _pendingImageCapture.value = PendingCapture(uri, context)
    }

    /**
     * Called by [ReceiptViewModel] after it processes the image.
     * Clears the pending capture.
     */
    fun clearPendingCapture() {
        _pendingImageCapture.value = null
    }

    fun setScanValidation(receiptID: Long, validation: ScanValidation) {
        _pendingScanValidation.value = PendingScanValidation(receiptID, validation)
    }

    fun clearScanValidation() {
        _pendingScanValidation.value = null
    }

    data class PendingCapture(
        val uri: Uri,
        val context: Context
    )
    data class PendingScanValidation(
        val receiptID: Long,
        val validation: ScanValidation
    )
}