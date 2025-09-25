package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation
import weberstudio.app.billigsteprodukter.ui.pages.home.MainPageContent

/**
 * The shell of each page in the app. Has to be applied to every page in the app
 * @param navController the page controller
 * @param title the title of the page
 * @param modifier the modifier that's going to be propagated to page
 * @param pageContent the content that has to be displayed on the page. F.ex. [MainPageContent] for the "Home" page
 * @param floatingActionButton an **optional** action button layered on top of the UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageShell(navController: NavHostController,
              title: String,
              modifier: Modifier = Modifier,
              pageContent: @Composable (PaddingValues) -> Unit,
              floatingActionButton: (@Composable () -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            //Only shows settings on home page
            if (title == "Hjem") {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate(PageNavigation.Settings.route)
                        }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Indstillinger"
                            )
                        }
                    },
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
            }
        },
        bottomBar = { NavigationBar(navController) },
        floatingActionButton = {
            if (floatingActionButton != null) floatingActionButton()
        },
    ) { innerPadding ->
        pageContent(innerPadding)
    }
}

@Composable
fun NavigationBar(navController: NavController) {
    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val cameraScope = rememberCoroutineScope()
        val cameraViewModel: CameraViewModel = viewModel()
        val launchCamera = launchCamera(
            onImageCaptured = { uri, context ->
                cameraScope.launch { cameraViewModel.processImage(uri, context) }
            }
        )

        //Home
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Hjem",
                    modifier = Modifier.size(36.dp),
                )
            },
            selected = currentRoute == PageNavigation.Home.route,
            onClick = {
                navController.navigate(PageNavigation.Home.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )

        //Shopping list
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.list),
                    contentDescription = "Shopping lister",
                    modifier = Modifier.size(36.dp),
                )
            },
            selected = currentRoute == PageNavigation.ShoppingList.route || currentRoute == PageNavigation.ShoppingListUndermenu.route,
            onClick = {
                navController.navigate(PageNavigation.ShoppingList.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        //Historik
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.pricetag_icon),
                    contentDescription = "Database",
                    modifier = Modifier.size(36.dp),
                )
            },
            selected = currentRoute == PageNavigation.Database.route,
            onClick = {
                navController.navigate(PageNavigation.Database.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        //Receipt scanning
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(R.drawable.camera_icon),
                    contentDescription = "Scan kvittering",
                    modifier = Modifier.size(36.dp),
                )
            },
            selected = currentRoute == PageNavigation.ReceiptScanning.route,
            onClick = { launchCamera() }
        )
    }
}
