package weberstudio.app.billigsteprodukter.ui.pages.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import weberstudio.app.billigsteprodukter.ui.components.MapUI
import weberstudio.app.billigsteprodukter.ui.components.QuickActionsUI
import weberstudio.app.billigsteprodukter.ui.components.SaveImageButton

/**
 * The UI content of the *Main* Page
 */
@Composable
fun MainPageContent(modifier: Modifier = Modifier, viewModel: MainPageViewModel = viewModel()) {
    //Main page
    Column(
        modifier = modifier
            .padding(12.dp) //Standard padding from screen edge
            //.border(1.dp, Color.Red, RoundedCornerShape(8.dp)) //Debug
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //Save receipt
        SaveImageButton(
            modifier = Modifier
                .weight(1.25f)
                .fillMaxSize()
            //.border(1.dp, Color.Gray, RoundedCornerShape(8.dp)) //Debug
        ) { bitmap ->
            viewModel.processImage(bitmap)
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
}