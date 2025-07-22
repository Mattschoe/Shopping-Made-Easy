package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.components.SaveImage
import androidx.compose.runtime.getValue
import weberstudio.app.billigsteprodukter.ui.ParsingState
import weberstudio.app.billigsteprodukter.ui.components.ErrorMessageLarge
import weberstudio.app.billigsteprodukter.ui.components.launchCamera
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation

/**
 * @param uiContent the UI that activates the [SaveImage] function
 */
@Composable
fun ReceiptScanningContent(modifier: Modifier = Modifier, navController: NavController, viewModel: CameraViewModel) {
    //region Checks for parsing errors first:
    val parsingState by viewModel.getParserState()
    val launchCamera = launchCamera(
        onImageCaptured = { uri, context -> viewModel.processImage(uri, context) },
        onImageProcessed = { navController.navigate(PageNavigation.ReceiptScanning.route) }
    )
    if (parsingState is ParsingState.InProgress) CircularProgressIndicator()
    else if (parsingState is ParsingState.Error) {
        val errorMessage = (parsingState as ParsingState.Error).message
        ErrorMessageLarge(
            "Fejl i scanning!",
            errorMessage,
            onDismissRequest = {
                viewModel.clearParserState()
                navController.navigate(PageNavigation.Home.route)
            },
            onConfirmError = { launchCamera() }, //Launches camera again if user clicks "Prøv igen"
            onDismissError = { } //Goes back to last screen if user presses "Cancel"
        )
    }
    //endregion

    
}