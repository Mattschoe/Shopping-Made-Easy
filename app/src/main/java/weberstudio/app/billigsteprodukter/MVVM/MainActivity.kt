package weberstudio.app.billigsteprodukter.MVVM

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.navigation.PageNavigation
import weberstudio.app.billigsteprodukter.ui.components.SaveImageButton
import weberstudio.app.billigsteprodukter.ui.theme.BilligsteProdukterTheme
import weberstudio.app.billigsteprodukter.ui.theme.ThemeLightGreen
import java.io.File

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ViewModel>()
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

    /**
     * The UI content of the *Main* Page
     */
    @Composable
    fun MainPageContent(modifier: Modifier = Modifier) {
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

    /**
     * The UI content of the *Settings* Page
     */
    @Composable
    fun SettingsPageContent(modifier: Modifier = Modifier) {
        //HEJ :)
    }

    /**
     * The layout UI for the quick actions buttons
     */
    @Composable
    fun QuickActionsUI(modifier: Modifier) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CreateShoppingListUI(
                modifier = Modifier
                    .weight(1f, fill = false)
            )

            TempUI(
                modifier = modifier
                    .weight(1f,  fill = false)
            )
        }
    }

    /**
     * Buttons for quick actions. Max 2 per row
     */
    @Composable
    fun QuickActionsButton(text: String, @DrawableRes iconRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier
                .height(72.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = ThemeLightGreen
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = "Ikon af kvittering",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(16.dp)) //Padding between icon and text

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    @Composable
    fun CreateShoppingListUI(modifier: Modifier) {
        QuickActionsButton("Opret indkøbsliste", R.drawable.list, { println("Jeg vil gerne oprette min indkøbsliste!") }, modifier)
    }

    @Composable
    fun MapUI(modifier: Modifier) {
        Box(
            modifier = modifier
        ) {
            Text(
                text = "Her skal der være et kort! :)",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    //Temporary name until i find a use for this
    @Composable
    fun TempUI(modifier: Modifier) {
        QuickActionsButton("Temp UI", R.drawable.list, { println("Jeg vil gerne lave noget temp her!") }, modifier)
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

