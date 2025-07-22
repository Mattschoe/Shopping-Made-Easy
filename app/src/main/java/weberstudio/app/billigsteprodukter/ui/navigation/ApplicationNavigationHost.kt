package weberstudio.app.billigsteprodukter.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.components.PageShell
import weberstudio.app.billigsteprodukter.ui.pages.database.DatabaseContent
import weberstudio.app.billigsteprodukter.ui.pages.home.MainPageContent
import weberstudio.app.billigsteprodukter.ui.pages.map.MapContent
import weberstudio.app.billigsteprodukter.ui.pages.receiptScanning.ReceiptScanningContent
import weberstudio.app.billigsteprodukter.ui.pages.settings.SettingsPageContent
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingListContent

/**
 * The host that's responsible for navigation between pages in the application
 */
@Composable
fun ApplicationNavigationHost(navController: NavHostController = rememberNavController(), startPage: String = PageNavigation.Home.route) {
    val cameraViewModel: CameraViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startPage,
        modifier = Modifier
            .fillMaxSize()
    ) {
        //Main Screen
        composable(PageNavigation.Home.route) {
            PageShell(navController, title = "Forside") { padding ->
                MainPageContent(Modifier.padding(padding), navController, cameraViewModel)
            }
        }

        //Receipt Content
        composable(PageNavigation.ReceiptScanning.route) {
            PageShell(navController, title = "Kvitteringsoversigt") { padding ->
                ReceiptScanningContent(Modifier.padding(padding), navController, cameraViewModel)
            }
        }

        //Shopping List
        composable(PageNavigation.ShoppingList.route) {
            PageShell(navController, title = "Indkøbsliste") { padding ->
                ShoppingListContent(Modifier.padding(padding))
            }
        }

        //Database
        composable(PageNavigation.Database.route) {
            PageShell(navController, title = "Oversigt over priser") { padding ->
                DatabaseContent(Modifier.padding(padding))
            }
        }

        //Map
        composable(PageNavigation.Map.route) {
            PageShell(navController, title = "Kort") { padding ->
                MapContent(Modifier.padding(padding))
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
