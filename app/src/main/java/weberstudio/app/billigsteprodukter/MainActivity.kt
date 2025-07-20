package weberstudio.app.billigsteprodukter

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation
import weberstudio.app.billigsteprodukter.ui.pages.home.MainPageContent
import weberstudio.app.billigsteprodukter.ui.pages.settings.SettingsPageContent
import weberstudio.app.billigsteprodukter.ui.theme.BilligsteProdukterTheme

class MainActivity : ComponentActivity() {
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Asks for permissions
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                // Handle Permission granted/rejected
                var permissionGranted = true
                permissions.entries.forEach {
                    if (it.key in REQUIRED_PERMISSIONS && it.value == false) permissionGranted = false //If permissions aren't granted
                }
                if (!permissionGranted) {
                    Toast.makeText(baseContext,
                        "Permission request denied",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext,
                        "Permissions accepted!",
                        Toast.LENGTH_SHORT).show()
                }
        }
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)

        setContent {
            BilligsteProdukterTheme {
                ApplicationNavigationHost()
            }
        }
    }

    /**
     * The host that's responsible for navigation between pages in the application
     */
    @Composable
    fun ApplicationNavigationHost(navController: NavHostController = rememberNavController(), startPage: String = PageNavigation.Home.route) {
        NavHost(
            navController = navController,
            startDestination = startPage,
            modifier = Modifier
                .fillMaxSize()
        ) {
            //Main Screen
            composable(PageNavigation.Home.route) {
                PageShell(navController, title = "Forside") { padding ->
                    MainPageContent(Modifier.padding(padding))
                }
            }

            //Settings
            composable(PageNavigation.Settings.route) {
                PageShell(navController, title = "Indstillinger") { padding ->
                    SettingsPageContent(Modifier.padding(padding))
                }
            }
        }
    }

    /**
     * The shell of each page in the app. Has to be applied to every page in the app
     * @param navController the page controller
     * @param title the title of the page
     * @param modifier the modifier that's going to be propagated to page
     * @param pageContent the content that has to be displayed on the page. F.ex. [MainPageContent] for the "Home" page
     */
    @Composable
    fun PageShell(navController: NavController, title: String, modifier: Modifier = Modifier, pageContent: @Composable (PaddingValues) -> Unit) {
        //Navigation drawer
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    NavigationDrawerUI(
                        onDestinationClicked = { destination ->
                            scope.launch { drawerState.close() } //Closes the nav drawer since we are changing page
                            /*when (destination) {
                                NavigationDestinations.Home -> navController.navigate("home")
                                NavigationDestinations.Settings -> navController.navigate("settings")
                            } */
                        }
                    )
                }
            },
            modifier = modifier
        ) {
            Scaffold(
                //Top bar UI
                topBar = {
                    NavigationUI(title, onMenuClick = { scope.launch { drawerState.open()} })
                }
            ) { innerPadding ->
                pageContent(innerPadding) //Shows the page given as parameter
            }
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
}

/**
 * The destinations/pages of the app
 */
enum class NavigationDestinations{Home, Settings}

