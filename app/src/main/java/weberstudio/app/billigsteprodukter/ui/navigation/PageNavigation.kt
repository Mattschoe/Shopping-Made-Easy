package weberstudio.app.billigsteprodukter.ui.navigation

import java.time.Month
import java.time.Year

sealed class PageNavigation(val route: String) {
    object Home : PageNavigation("home")
    object ReceiptScanning : PageNavigation("receipt/{ID}")
    object ShoppingList : PageNavigation("shoppingList")
    object ShoppingListUndermenu : PageNavigation("shoppingListDetail/{listID}")
    object Database : PageNavigation("database")
    object Budget : PageNavigation("budget/{year}/{month}")
    object Settings : PageNavigation("settings")

    companion object {
        fun createShoppingListDetailRoute(listID: String) = "shoppingListDetail/$listID"
        fun createBudgetRoute(month: Month, year: Year) = "budget/$year/$month"
        fun createReceiptRoute(receiptID: Long) = "receipt/$receiptID"
    }
}