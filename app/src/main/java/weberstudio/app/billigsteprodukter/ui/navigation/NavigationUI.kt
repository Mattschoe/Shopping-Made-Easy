package weberstudio.app.billigsteprodukter.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.ui.components.QuickActionsButton

/***
 * The UI for the navigation drawer
 * @param onDestinationClicked the destination chosen by user, destinations are stated in enum "NavigationDestinations"
 */
@Composable
fun NavigationDrawerUI(modifier: Modifier = Modifier, onDestinationClicked: (NavigationDestinations) -> Unit) {
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
            { onDestinationClicked(NavigationDestinations.Home) },
            modifier
        )
        QuickActionsButton(
            "Scan kvittering",
            R.drawable.camera_icon,
            { onDestinationClicked(NavigationDestinations.ReceiptScanning) },
            modifier
        )
        QuickActionsButton(
            "Opret Indkøbsliste",
            R.drawable.menu_dots_svgrepo_com,
            { onDestinationClicked(NavigationDestinations.ShoppingList) },
            modifier
        )
        QuickActionsButton(
            "Oversigt over priser",
            R.drawable.pricetag_icon,
            { onDestinationClicked(NavigationDestinations.Database) },
            modifier
        )
        QuickActionsButton(
            "Kort",
            R.drawable.map_icon,
            { onDestinationClicked(NavigationDestinations.Map) },
            modifier
        )

        //Bottom
        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider()
        QuickActionsButton("Indstillinger", R.drawable.settings_icon, { onDestinationClicked(NavigationDestinations.Settings) }, modifier)
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
                    painterResource(id = R.drawable.menu_dots_svgrepo_com),
                    contentDescription = "Åbne navigationsmenu"
                )
            }
        }
    )
}