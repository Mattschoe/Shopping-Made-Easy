package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ui.navigation.NavigationDrawerUI
import weberstudio.app.billigsteprodukter.ui.navigation.NavigationUI
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation
import weberstudio.app.billigsteprodukter.ui.pages.home.MainPageContent

/**
 * The shell of each page in the app. Has to be applied to every page in the app
 * @param navController the page controller
 * @param title the title of the page
 * @param modifier the modifier that's going to be propagated to page
 * @param pageContent the content that has to be displayed on the page. F.ex. [MainPageContent] for the "Home" page
 */
@Composable
fun PageShell(navController: NavHostController, title: String, modifier: Modifier = Modifier, pageContent: @Composable (PaddingValues) -> Unit) {
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
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true } //Popper back stacken så vi ikke får overflow
                            launchSingleTop = true
                            restoreState = true
                        }
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
