package weberstudio.app.billigsteprodukter.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import weberstudio.app.billigsteprodukter.R

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

/***
 * The UI for the navigation drawer
 * @param onDestinationClicked the destination chosen by user, destinations are stated in enum "NavigationDestinations"
 */
@Composable
fun NavigationDrawerUI(modifier: Modifier = Modifier, onDestinationClicked: (NavigationDestinations) -> Unit) {
    Column(
        modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        Text("Hjem",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDestinationClicked(NavigationDestinations.Home) }
                .padding(vertical = 8.dp)
        )
        HorizontalDivider()
        Text("Indstillinger",
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDestinationClicked(NavigationDestinations.Settings)}
                .padding(vertical = 8.dp)
        )
    }
}