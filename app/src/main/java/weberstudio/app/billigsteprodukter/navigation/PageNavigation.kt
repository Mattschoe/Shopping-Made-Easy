package weberstudio.app.billigsteprodukter.navigation

sealed class PageNavigation(val route: String) {
    object Home : PageNavigation("home")
    object Settings : PageNavigation("settings")
}