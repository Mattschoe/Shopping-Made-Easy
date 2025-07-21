package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.components.SaveImage

@Composable
fun ReceiptScanningContent(modifier: Modifier = Modifier, cameraViewModel: CameraViewModel = CameraViewModel()) {
    SaveImage{ bitmap -> cameraViewModel.processImage(bitmap) }
}