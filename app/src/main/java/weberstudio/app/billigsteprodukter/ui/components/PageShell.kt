package weberstudio.app.billigsteprodukter.ui.components

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.logic.CameraCoordinator
import weberstudio.app.billigsteprodukter.logic.Logger
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
fun PageShell(
    navController: NavHostController,
    title: String,
    modifier: Modifier = Modifier,
    pageContent: @Composable (PaddingValues) -> Unit,
    floatingActionButton: (@Composable () -> Unit)? = null,
) {
    val context = LocalContext.current
    val cameraCoordinator: CameraCoordinator = viewModel(
        viewModelStoreOwner = context as ComponentActivity
    )

    // Hoist camera state to PageShell level
    var showCamera by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
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
            bottomBar = {
                NavigationBar(
                    navController = navController,
                    onLaunchCamera = { showCamera = true }
                )
            },
            floatingActionButton = {
                if (floatingActionButton != null) floatingActionButton()
            },
        ) { innerPadding ->
            pageContent(innerPadding)
        }

        // Camera overlay - renders on top of everything
        if (showCamera) {
            CameraWithFlashlight(
                onImageCaptured = { uri, ctx ->
                    cameraCoordinator.onImageCaptured(uri, ctx)
                    showCamera = false
                    navController.navigate(PageNavigation.createReceiptRoute(0))
                },
                onError = { exception ->
                    Logger.log("Camera", "TakePicture Failure! ${exception.message}")
                    Toast.makeText(context, "Image capture failed!", Toast.LENGTH_SHORT).show()
                    showCamera = false
                },
                onDismiss = {
                    showCamera = false
                }
            )
        }
    }
}

@Composable
fun NavigationBar(
    navController: NavController,
    onLaunchCamera: () -> Unit
) {
    //region SCANNING VALIDATION
    val settingsRepo = (LocalContext.current.applicationContext as ReceiptApp).settingsRepository
    val scope = rememberCoroutineScope()
    var showCoop365OptionsDialog by remember { mutableStateOf(false) }
    //endregion

    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

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
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )

        //Shopping list
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.list_icon),
                    contentDescription = "Shopping lister",
                    modifier = Modifier.size(36.dp),
                )
            },
            selected = currentRoute == PageNavigation.ShoppingList.route || currentRoute == PageNavigation.ShoppingListUndermenu.route,
            onClick = {
                navController.navigate(PageNavigation.ShoppingList.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        //Historik
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.pricetag_icom),
                    contentDescription = "Database",
                    modifier = Modifier.size(36.dp),
                )
            },
            selected = currentRoute == PageNavigation.Database.route,
            onClick = {
                navController.navigate(PageNavigation.Database.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
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
            selected = currentRoute?.startsWith("receipt/") == true,
            onClick = {
                scope.launch {
                    val coopOption = settingsRepo.coop365Option.firstOrNull()
                    if (coopOption == null) showCoop365OptionsDialog = true
                    else onLaunchCamera()
                }
            }
        )
    }

    //region DIALOGS
    if (showCoop365OptionsDialog) {
        Coop365OptionDialog(
            onDismiss = { showCoop365OptionsDialog = false },
            onConfirm = { option ->
                scope.launch {
                    settingsRepo.setCoop365Option(option)
                    showCoop365OptionsDialog = false
                    onLaunchCamera()
                }
            }
        )
    }
    //endregion
}

