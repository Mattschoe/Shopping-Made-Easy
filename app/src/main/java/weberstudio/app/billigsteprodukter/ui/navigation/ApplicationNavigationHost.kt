package weberstudio.app.billigsteprodukter.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.ui.components.AddFAB
import weberstudio.app.billigsteprodukter.ui.components.AddProductToListDialog
import weberstudio.app.billigsteprodukter.ui.components.PageShell
import weberstudio.app.billigsteprodukter.ui.pages.database.DataBaseViewModel
import weberstudio.app.billigsteprodukter.ui.pages.database.DatabaseContent
import weberstudio.app.billigsteprodukter.ui.pages.home.MainPageContent
import weberstudio.app.billigsteprodukter.ui.pages.map.MapContent
import weberstudio.app.billigsteprodukter.ui.pages.receiptScanning.ReceiptScanningContent
import weberstudio.app.billigsteprodukter.ui.pages.receiptScanning.ReceiptScanningViewModel
import weberstudio.app.billigsteprodukter.ui.pages.settings.SettingsPageContent
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingListContent
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingListViewModel

/**
 * The host that's responsible for navigation between pages in the application
 */
@Composable
fun ApplicationNavigationHost(navController: NavHostController = rememberNavController(), startPage: String = PageNavigation.ReceiptRoute.route) {
    NavHost(
        navController = navController,
        startDestination = startPage,
        modifier = Modifier
            .fillMaxSize()
    ) {
        //Main Screen
        composable(PageNavigation.Home.route) { backStackEntry ->
            val cameraViewModel: CameraViewModel = viewModel()
            PageShell(
                navController,
                title = "Forside",
                pageContent =  { padding -> MainPageContent(Modifier.padding(padding), navController, cameraViewModel) }
            )
        }

        //Shopping List
        composable(PageNavigation.ShoppingList.route) { backStackEntry ->
            val viewModel: ShoppingListViewModel = viewModel()
            var showAddDialog by rememberSaveable { mutableStateOf(false) }

            PageShell(
                navController,
                title = "Indkøbsliste",
                pageContent = { padding -> ShoppingListContent(Modifier.padding(padding), viewModel) },
                floatingActionButton = { AddFAB(onClick = { showAddDialog = true }) }
            )

            AddProductToListDialog(
                showDialog = showAddDialog,
                onDismiss =  { showAddDialog = false },
                onConfirm =  { name, store ->
                    viewModel.addProduct(name, store)
                    showAddDialog = false
                }
            )
        }

        //Database
        composable(PageNavigation.Database.route) { backStackEntry ->
            val databaseViewModel: DataBaseViewModel = viewModel(backStackEntry)
            PageShell(
                navController,
                title = "Oversigt over priser",
                pageContent =  { padding -> DatabaseContent(Modifier.padding(padding), databaseViewModel) }
            )
        }

        //Map
        composable(PageNavigation.Map.route) {
            PageShell(
                navController,
                title = "Kort",
                pageContent = { padding -> MapContent(Modifier.padding(padding)) }
            )
        }

        //Settings
        composable(PageNavigation.Settings.route) {
            PageShell(
                navController,
                title = "Indstillinger",
                pageContent = { padding -> SettingsPageContent(Modifier.padding(padding)) }
            )
        }

        //Receipt navigation route
        navigation(
            route = "receiptRoute",
            startDestination = PageNavigation.ReceiptHome.route
        ) {
            //Receipt Main page
            composable(PageNavigation.ReceiptHome.route) { backStackEntry ->
                val parentBackStackEntry = remember(backStackEntry) { navController.getBackStackEntry("receiptRoute") }
                val cameraViewModel: CameraViewModel = viewModel(parentBackStackEntry)
                PageShell(
                    navController,
                    title = "Forside",
                    pageContent = { padding -> MainPageContent(Modifier.padding(padding), navController, cameraViewModel) }
                )
            }

            //Receipt Content
            composable(PageNavigation.ReceiptScanning.route) { backStackEntry ->
                val parentBackStackEntry = remember(backStackEntry) { navController.getBackStackEntry("receiptRoute") }
                val cameraViewModel: CameraViewModel = viewModel(parentBackStackEntry)
                PageShell(
                    navController,
                    title = "Kvitteringsoversigt",
                    pageContent = { padding ->
                        val receiptViewModel: ReceiptScanningViewModel = viewModel(backStackEntry)
                        ReceiptScanningContent(Modifier.padding(padding), navController, cameraViewModel, receiptViewModel)
                    }
                )
            }
        }
    }
}
