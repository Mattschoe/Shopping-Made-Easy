package weberstudio.app.billigsteprodukter.ui.pages.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.ParsingState
import weberstudio.app.billigsteprodukter.ui.components.AddProductDialog
import weberstudio.app.billigsteprodukter.ui.components.ErrorMessageLarge
import weberstudio.app.billigsteprodukter.ui.components.LatestActivityButton
import weberstudio.app.billigsteprodukter.ui.components.launchCamera
import weberstudio.app.billigsteprodukter.ui.components.MapUI
import weberstudio.app.billigsteprodukter.ui.components.QuickActionsUI
import weberstudio.app.billigsteprodukter.ui.components.SaveImage
import weberstudio.app.billigsteprodukter.ui.components.SaveImageButton
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation

/**
 * The UI content of the *Main* Page
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainPageContent(modifier: Modifier = Modifier, navController: NavController, viewModel: CameraViewModel) {
    val cameraScope = rememberCoroutineScope()

    //Main page
    Column(
        modifier = modifier
            .padding(12.dp) //Standard padding from screen edge
            .fillMaxSize()
            //.border(2.dp, Color.Magenta)
                ,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //Save receipt
        SaveImage(
            onImageCaptured = { uri, context ->
                cameraScope.launch {
                    viewModel.processImage(uri, context)
                    navController.navigate(PageNavigation.ReceiptScanning.route)
                }
            }
        ) { modifier, launchCamera ->
            SaveImageButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    //.border(2.dp, Color.Red)
                        ,
                onClick = launchCamera
            )
        }

        //Quick actions row
        QuickActionsUI(
            modifier = Modifier
                .wrapContentSize(align = Alignment.BottomCenter)
                //.border(2.dp, Color.Blue)
                    ,
            onClick = { pageNav ->
                navController.navigate(pageNav.route)
            }
        )

        //region Latest activity
        Text(
            text = "Seneste Aktivitet",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )


        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, Color.Black)
        ) {
            items(10) { _ ->
                LatestActivityButton("Hejsa")
            }
        }
        //endregion
    }
}