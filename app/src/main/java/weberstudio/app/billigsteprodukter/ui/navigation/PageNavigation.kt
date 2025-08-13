package weberstudio.app.billigsteprodukter.ui.navigation

sealed class PageNavigation(val route: String) {
    object Home : PageNavigation("home")
    object ReceiptHome : PageNavigation("receiptHome")
    object ReceiptScanning : PageNavigation("receipt")
    object ShoppingList : PageNavigation("shoppingList")
    object ShoppingListUndermenu : PageNavigation("shoppingListDetail/{listId}")
    object Database : PageNavigation("database")
    object Map : PageNavigation("map")
    object Settings : PageNavigation("settings")
    object ReceiptRoute : PageNavigation("receiptRoute")

    companion object {
        fun createShoppingListDetailRoute(listId: String) = "shoppingListDetail/$listId"
    }
}