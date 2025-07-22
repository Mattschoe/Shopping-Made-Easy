package weberstudio.app.billigsteprodukter.ui.pages.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.ParsingState
import weberstudio.app.billigsteprodukter.ui.components.ErrorMessageLarge
import weberstudio.app.billigsteprodukter.ui.components.launchCamera
import weberstudio.app.billigsteprodukter.ui.components.MapUI
import weberstudio.app.billigsteprodukter.ui.components.QuickActionsUI
import weberstudio.app.billigsteprodukter.ui.components.SaveImage
import weberstudio.app.billigsteprodukter.ui.components.SaveImageButton
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation

/**
 * The UI content of the *Main* Page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageContent(modifier: Modifier = Modifier, navController: NavController, viewModel: CameraViewModel) {
    val parsingState by viewModel.getParserState()
    val launchCamera = launchCamera(
        onImageCaptured = { uri, context -> viewModel.processImage(uri, context) },
        onImageProcessed = { navController.navigate(PageNavigation.ReceiptScanning.route) }
    )

    //Main page
    Column(
        modifier = modifier
            .padding(12.dp) //Standard padding from screen edge
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //Save receipt
        SaveImage(
            onImageCaptured = { uri, context -> viewModel.processImage(uri, context) },
            onImageProcessed = {
                try {
                    navController.navigate(PageNavigation.ReceiptScanning.route)
                } catch (e: Exception) {
                    println(e)
                } }
        ) { modifier, launchCamera ->
            SaveImageButton(
                modifier = Modifier
                    .weight(1.25f)
                    .fillMaxSize(),
                onClick = launchCamera
            )
        }

        //Quick actions row
        QuickActionsUI(
            modifier = Modifier
                .wrapContentSize(align = Alignment.BottomCenter)
        )

        //Map UI
        MapUI(
            modifier = Modifier
                .weight(0.75f)
                .fillMaxWidth()
        )
    }

    //Checks for error in parsing
    if (parsingState is ParsingState.InProgress) CircularProgressIndicator()
    else if (parsingState is ParsingState.Error) {
        val errorMessage = (parsingState as ParsingState.Error).message
        ErrorMessageLarge(
            "Fejl i scanning!",
            errorMessage,
            onDismissRequest = {
                viewModel.clearParserState()
                navController.popBackStack()
           },
            onConfirmError = { launchCamera() }, //Launches camera again if user clicks "Prøv igen"
            onDismissError = { } //Goes back to last screen if user presses "Cancel"
        )
    }
}