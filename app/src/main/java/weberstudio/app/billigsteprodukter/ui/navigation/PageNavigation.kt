package weberstudio.app.billigsteprodukter.ui.navigation

sealed class PageNavigation(val route: String) {
    object Home : PageNavigation("home")
    object ReceiptScanning : PageNavigation("receiptScanning")
    object ShoppingList : PageNavigation("shoppingList")
    object Database : PageNavigation("database")
    object Map : PageNavigation("map")
    object Settings : PageNavigation("settings")
}