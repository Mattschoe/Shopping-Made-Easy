package weberstudio.app.billigsteprodukter.ui.pages.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.components.MapUI
import weberstudio.app.billigsteprodukter.ui.components.QuickActionsUI
import weberstudio.app.billigsteprodukter.ui.components.SaveImage
import weberstudio.app.billigsteprodukter.ui.components.SaveImageButton
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import weberstudio.app.billigsteprodukter.ui.ParsingState
import weberstudio.app.billigsteprodukter.ui.components.ErrorMessageLarge

/**
 * The UI content of the *Main* Page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageContent(modifier: Modifier = Modifier, navController: NavController, viewModel: CameraViewModel = viewModel()) {
    val parsingState by viewModel.getParserState()

    //Main page
    Column(
        modifier = modifier
            .padding(12.dp) //Standard padding from screen edge
            //.border(1.dp, Color.Red, RoundedCornerShape(8.dp)) //Debug
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //Save receipt
        SaveImage(
            onImageCaptured = { uri, context -> viewModel.processImage(uri, context) },
            onImageProcessed = { print("Hej:)")}
            //onImageProcessed = { navController.navigate(PageNavigation.ReceiptScanning) }
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
            //.border(1.dp, Color.Gray, RoundedCornerShape(8.dp)) //Debug
        )

        //Map UI
        MapUI(
            modifier = Modifier
                .weight(0.75f)
                .fillMaxWidth()
            //.border(1.dp, Color.Gray, RoundedCornerShape(8.dp)) //Debug
        )
    }

    //Checks for error in parsing
    if (parsingState is ParsingState.InProgress) CircularProgressIndicator()
    else if (parsingState is ParsingState.Error) {
        val errorMessage = (parsingState as ParsingState.Error).message
        ErrorMessageLarge(
            { viewModel.clearParserState() },
            "Fejl i scanning!",
            errorMessage,
            { viewModel.clearParserState() }
        )


        println(errorMessage)
        AlertDialog(
            onDismissRequest = { viewModel.clearParserState() },
            title = { Text("Fejl i scanning!") },
            text = { Text(errorMessage)},
            confirmButton = {
                TextButton(onClick = { viewModel.clearParserState() }) {
                    Text("Ok")
                }
            }
        )
    }
}

@Composable
fun ParserErrorHandler() {

}