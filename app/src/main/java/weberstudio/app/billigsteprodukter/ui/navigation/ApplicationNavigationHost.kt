package weberstudio.app.billigsteprodukter.ui.navigation

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.components.AddFAB
import weberstudio.app.billigsteprodukter.ui.components.AddProductToListDialog
import weberstudio.app.billigsteprodukter.ui.components.PageShell
import weberstudio.app.billigsteprodukter.ui.pages.database.DataBaseViewModel
import weberstudio.app.billigsteprodukter.ui.pages.database.DatabaseContent
import weberstudio.app.billigsteprodukter.ui.pages.home.MainPageContent
import weberstudio.app.billigsteprodukter.ui.pages.budget.BudgetPage
import weberstudio.app.billigsteprodukter.ui.pages.budget.BudgetViewModel
import weberstudio.app.billigsteprodukter.ui.pages.receiptScanning.ReceiptScanningContent
import weberstudio.app.billigsteprodukter.ui.pages.receiptScanning.ReceiptScanningViewModel
import weberstudio.app.billigsteprodukter.ui.pages.settings.SettingsPageContent
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingListUndermenuContent
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingListUndermenuViewModel
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingListsPage
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingListsViewModel

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
            val context = LocalContext.current
            val cameraViewModel: CameraViewModel = viewModel(backStackEntry) {
                CameraViewModel(context.applicationContext as Application)
            }
            val budgetViewModel: BudgetViewModel = viewModel(backStackEntry) {
                BudgetViewModel(context.applicationContext as Application)
            }
            PageShell(
                navController,
                title = "Forside",
                pageContent =  { padding -> MainPageContent(Modifier.padding(padding), navController, cameraViewModel, budgetViewModel) }
            )
        }

        //Shopping List Main menu
        composable(PageNavigation.ShoppingList.route) { backStackEntry ->
            val context = LocalContext.current
            val viewModel: ShoppingListsViewModel = viewModel(backStackEntry) {
                ShoppingListsViewModel(context.applicationContext as Application)
            }
            PageShell(
                navController,
                title = "Indkøbslister",
                pageContent = { padding ->
                    ShoppingListsPage(
                        modifier = Modifier.padding(padding),
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            )
        }

        //Shopping List Under menu
        composable(
            PageNavigation.ShoppingListUndermenu.route,
            arguments = listOf(navArgument("listID") { type = NavType.StringType})
        ) { backStackEntry ->
            val context = LocalContext.current
            val viewModel: ShoppingListUndermenuViewModel = viewModel(backStackEntry) {
                ShoppingListUndermenuViewModel(context.applicationContext as Application)
            }
            val listID = backStackEntry.arguments?.getString("listID")
            val shoppingListName = if (listID != null) viewModel.getShoppingListName(listID) else "Indkøbsliste"
            var showAddDialog by rememberSaveable { mutableStateOf(false) }


            PageShell(
                navController,
                title = "$shoppingListName",
                pageContent = { padding ->
                    ShoppingListUndermenuContent(
                        listID = listID,
                        modifier = Modifier.padding(padding),
                        navController = navController,
                        viewModel = viewModel
                    )
                },
                floatingActionButton = { AddFAB(onClick = { showAddDialog = true}) }
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
                title = "Oversigt",
                pageContent =  { padding -> DatabaseContent(Modifier.padding(padding), databaseViewModel) }
            )
        }

        //Budget
        composable(PageNavigation.Budget.route) { backStackEntry ->
            val budgetViewModel: BudgetViewModel = viewModel(backStackEntry)
            PageShell(
                navController,
                title = "Budget",
                pageContent = { padding -> BudgetPage(Modifier.padding(padding), budgetViewModel) }
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
                val context = LocalContext.current
                val cameraViewModel: CameraViewModel = viewModel(parentBackStackEntry) {
                    CameraViewModel(context.applicationContext as Application)
                }
                val budgetViewModel: BudgetViewModel = viewModel(backStackEntry) {
                    BudgetViewModel(context.applicationContext as Application)
                }
                PageShell(
                    navController,
                    title = "Forside",
                    pageContent = { padding -> MainPageContent(Modifier.padding(padding), navController, cameraViewModel, budgetViewModel) }
                )
            }

            //Receipt Content
            composable(PageNavigation.ReceiptScanning.route) { backStackEntry ->
                val parentBackStackEntry = remember(backStackEntry) { navController.getBackStackEntry("receiptRoute") }
                val context = LocalContext.current
                val cameraViewModel: CameraViewModel = viewModel(parentBackStackEntry) {
                    CameraViewModel(context.applicationContext as Application)
                }
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
