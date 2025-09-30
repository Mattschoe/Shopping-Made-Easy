package weberstudio.app.billigsteprodukter.ui.navigation

import java.time.Month
import java.time.Year

sealed class PageNavigation(val route: String) {
    object Home : PageNavigation("home")
    object ReceiptHome : PageNavigation("receiptHome")
    object ReceiptScanning : PageNavigation("receipt")
    object ShoppingList : PageNavigation("shoppingList")
    object ShoppingListUndermenu : PageNavigation("shoppingListDetail/{listID}")
    object Database : PageNavigation("database")
    object Budget : PageNavigation("budget/{year}/{month}")
    object Settings : PageNavigation("settings")
    object ReceiptRoute : PageNavigation("receiptRoute")

    companion object {
        fun createShoppingListDetailRoute(listID: String) = "shoppingListDetail/$listID"
        fun createBudgetRoute(month: Month, year: Year) = "budget/$year/$month"
    }
}