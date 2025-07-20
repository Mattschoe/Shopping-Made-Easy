package weberstudio.app.billigsteprodukter.ui.navigation

sealed class PageNavigation(val route: String) {
    object Home : PageNavigation("home")
    object Settings : PageNavigation("settings")
}