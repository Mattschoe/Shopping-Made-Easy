package weberstudio.app.billigsteprodukter.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.components.QuickActionsButton
import weberstudio.app.billigsteprodukter.ui.components.SaveImage

/***
 * The UI for the navigation drawer
 * @param onDestinationClicked the destination chosen by user, destinations are stated in enum "NavigationDestinations"
 */
@Composable
fun NavigationDrawerUI(modifier: Modifier = Modifier, onDestinationClicked: (PageNavigation) -> Unit) {
    val cameraScope = rememberCoroutineScope()
    val cameraViewModel: CameraViewModel = viewModel()

    Column(
        modifier
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //Top (Title)
        Row {
            Text(
                "TITEL",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                fontSize = 48.sp
            )
        }
        HorizontalDivider()

        //Middle
        QuickActionsButton(
            "Hjem",
            R.drawable.home_icon,
            { onDestinationClicked(PageNavigation.Home) },
            modifier
        )

        //"Scan kvittering"
        SaveImage(
            onImageCaptured = {
                uri, context -> cameraScope.launch {
                    cameraViewModel.processImage(uri, context)
                    onDestinationClicked(PageNavigation.ReceiptScanning)
                }
            }
        ) { modifier, launchCamera ->
            QuickActionsButton(
                "Scan kvittering",
                R.drawable.camera_icon,
                onClick = launchCamera,
                modifier = modifier
            )
        }

        QuickActionsButton(
            "Opret Indkøbsliste",
            R.drawable.threedots_icon,
            { onDestinationClicked(PageNavigation.ShoppingList) },
            modifier
        )
        QuickActionsButton(
            "Oversigt over priser",
            R.drawable.pricetag_icon,
            { onDestinationClicked(PageNavigation.Database) },
            modifier
        )
        QuickActionsButton(
            "Kort",
            R.drawable.map_icon,
            { onDestinationClicked(PageNavigation.Map) },
            modifier
        )

        //Bottom
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider()
        QuickActionsButton("Indstillinger", R.drawable.settings_icon, { onDestinationClicked(PageNavigation.Settings) }, modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationUI(title: String, onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painterResource(id = R.drawable.threedots_icon),
                    contentDescription = "Åbne navigationsmenu"
                )
            }
        }
    )
}