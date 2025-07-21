package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.components.SaveImage

/**
 * @param uiContent the UI that activates the [SaveImage] function
 */
@Composable
fun ReceiptScanningContent(cameraViewModel: CameraViewModel = CameraViewModel(), uiContent: @Composable (modifier: Modifier, onClick: () -> Unit) -> Unit) {
    SaveImage(
        onImageCaptured = { bitmap -> cameraViewModel.processImage(bitmap) },
        uiContent
    )
}