package weberstudio.app.billigsteprodukter.ui.navigation

sealed class PageNavigation(val route: String) {
    object Home : PageNavigation("home")
    object ReceiptHome : PageNavigation("receiptHome")
    object ReceiptScanning : PageNavigation("receipt")
    object ShoppingList : PageNavigation("shoppingList")
    object ShoppingListUndermenu : PageNavigation("shoppingListDetail/{listID}")
    object Database : PageNavigation("database")
    object Map : PageNavigation("map")
    object Settings : PageNavigation("settings")
    object ReceiptRoute : PageNavigation("receiptRoute")

    companion object {
        fun createShoppingListDetailRoute(listID: String) = "shoppingListDetail/$listID"
    }
}