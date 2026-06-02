package weberstudio.app.billigsteprodukter.ui.navigation

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import weberstudio.app.billigsteprodukter.logic.ActivityViewModel
import weberstudio.app.billigsteprodukter.logic.CameraCoordinator
import weberstudio.app.billigsteprodukter.ui.components.AddFAB
import weberstudio.app.billigsteprodukter.ui.components.AddProductToListDialog
import weberstudio.app.billigsteprodukter.ui.components.AddShoppingListDialog
import weberstudio.app.billigsteprodukter.ui.components.PageShell
import weberstudio.app.billigsteprodukter.ui.pages.budget.BudgetPage
import weberstudio.app.billigsteprodukter.ui.pages.budget.BudgetViewModel
import weberstudio.app.billigsteprodukter.ui.pages.database.DataBaseViewModel
import weberstudio.app.billigsteprodukter.ui.pages.database.DatabaseContent
import weberstudio.app.billigsteprodukter.ui.pages.home.MainPageContent
import weberstudio.app.billigsteprodukter.ui.pages.home.MainPageViewModel
import weberstudio.app.billigsteprodukter.ui.pages.receiptScanning.ReceiptScanningContent
import weberstudio.app.billigsteprodukter.ui.pages.receiptScanning.ReceiptViewModel
import weberstudio.app.billigsteprodukter.ui.pages.settings.SettingsPageContent
import weberstudio.app.billigsteprodukter.ui.pages.settings.SettingsViewModel
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingListUndermenuContent
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingListUndermenuViewModel
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingListsPage
import weberstudio.app.billigsteprodukter.ui.pages.shoppingList.ShoppingListsViewModel
import java.time.Month
import java.time.Year

/**
 * The host that's responsible for navigation between pages in the application
 */
@Composable
fun ApplicationNavigationHost(
    navController: NavHostController = rememberNavController(),
    startDestination: PageNavigation = PageNavigation.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        // Main Screen
        composable<PageNavigation.Home> { backStackEntry ->
            val context = LocalContext.current.applicationContext as Application
            val budgetViewModel: BudgetViewModel = viewModel(backStackEntry) {
                BudgetViewModel(context)
            }
            val mainPageViewModel: MainPageViewModel = viewModel(backStackEntry) {
                MainPageViewModel(context)
            }
            val activityViewModel: ActivityViewModel = viewModel(backStackEntry) {
                ActivityViewModel(context)
            }
            PageShell(
                navController,
                title = "Hjem",
                pageContent = { padding ->
                    MainPageContent(
                        modifier = Modifier.padding(padding),
                        navController = navController,
                        budgetViewModel = budgetViewModel,
                        mainPageViewModel = mainPageViewModel,
                        activityViewModel = activityViewModel
                    )
                }
            )
        }

        // Shopping List Main menu
        composable<PageNavigation.ShoppingList> { backStackEntry ->
            val context = LocalContext.current
            val viewModel: ShoppingListsViewModel = viewModel(backStackEntry) {
                ShoppingListsViewModel(context.applicationContext as Application)
            }
            var showAddShoppingList by remember { mutableStateOf(false) }

            PageShell(
                navController,
                title = "Indkøbslister",
                pageContent = { padding ->
                    ShoppingListsPage(
                        modifier = Modifier.padding(padding),
                        navController = navController,
                        viewModel = viewModel
                    )
                },
                floatingActionButton = { AddFAB(onClick = { showAddShoppingList = true }) }
            )

            AddShoppingListDialog(
                showDialog = showAddShoppingList,
                onDismiss = { showAddShoppingList = false },
                onConfirm = { name ->
                    viewModel.addShoppingList(name)
                    showAddShoppingList = false
                }
            )
        }

        //Shopping List Under menu
        composable<PageNavigation.ShoppingListUndermenu> { backStackEntry ->
            val context = LocalContext.current
            val viewModel: ShoppingListUndermenuViewModel = viewModel(backStackEntry) {
                ShoppingListUndermenuViewModel(context.applicationContext as Application)
            }
            val listID = backStackEntry.toRoute<PageNavigation.ShoppingListUndermenu>().listID
            viewModel.selectShoppingList(listID)
            var showAddDialog by rememberSaveable { mutableStateOf(false) }

            PageShell(
                navController,
                title = "Indkøbsliste",
                pageContent = { padding ->
                    ShoppingListUndermenuContent(
                        modifier = Modifier.padding(padding),
                        viewModel = viewModel
                    )
                },
                floatingActionButton = { AddFAB(onClick = { showAddDialog = true }) }
            )

            val searchResults by viewModel.searchResults.collectAsState()
            AddProductToListDialog(
                showDialog = showAddDialog,
                onDismiss = { showAddDialog = false },
                onConfirm = { name, store ->
                    viewModel.addCustomProductToList(name, store)
                    showAddDialog = false
                },
                searchResults = searchResults,
                onSearchQueryChange = viewModel::searchProductsInDatabase,
                onSelectExistingProduct = { product ->
                    viewModel.addExistingProductToList(product)
                    showAddDialog
                }
            )
        }

        // Database
        composable<PageNavigation.Database> { backStackEntry ->
            val databaseViewModel: DataBaseViewModel = viewModel(backStackEntry)
            PageShell(
                navController,
                title = "Oversigt",
                pageContent = { padding ->
                    DatabaseContent(Modifier.padding(padding), databaseViewModel)
                }
            )
        }

        // Budget
        composable<PageNavigation.Budget> { backStackEntry ->
            val budgetViewModel: BudgetViewModel = viewModel(backStackEntry)
            val route = backStackEntry.toRoute<PageNavigation.Budget>()
            // Klem måneden ind i 1-12, så en ugyldig værdi falder tilbage frem for at crashe (Month.of kaster ellers)
            val month = Month.of(route.month.coerceIn(1, 12))
            val year = Year.of(route.year)

            PageShell(
                navController,
                title = "Budget",
                pageContent = { padding ->
                    BudgetPage(Modifier.padding(padding), budgetViewModel, month, year)
                }
            )
        }

        // Settings
        composable<PageNavigation.Settings> { backStackEntry ->
            val viewModel: SettingsViewModel = viewModel(backStackEntry)
            PageShell(
                navController,
                title = "Indstillinger",
                pageContent = { padding ->
                    SettingsPageContent(Modifier.padding(padding), viewModel)
                }
            )
        }

        // Receipt Scanning
        composable<PageNavigation.ReceiptScanning> { backStackEntry ->
            val context = LocalContext.current
            val receiptID = backStackEntry.toRoute<PageNavigation.ReceiptScanning>().id
            val receiptViewModel: ReceiptViewModel = viewModel(
                viewModelStoreOwner = context as ComponentActivity
            )
            val cameraCoordinator: CameraCoordinator = viewModel(
                viewModelStoreOwner = context
            )

            LaunchedEffect(receiptID) {
                if (receiptID > 0) {
                    receiptViewModel.loadReceipt(receiptID)
                } else {
                    receiptViewModel.showLoadingState()
                }
            }

            PageShell(
                navController,
                title = "Oversigt",
                pageContent = { padding ->
                    ReceiptScanningContent(
                        modifier = Modifier.padding(padding),
                        navController = navController,
                        viewModel = receiptViewModel,
                        cameraCoordinator = cameraCoordinator
                    )
                }
            )
        }
    }
}
