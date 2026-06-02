package weberstudio.app.billigsteprodukter.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes. Each page is a [Serializable] route type; routes
 * with arguments carry them as typed constructor fields, read back in the host
 * via `backStackEntry.toRoute<T>()`.
 */
sealed interface PageNavigation {
    @Serializable
    data object Home : PageNavigation

    /** [id] = 0 starts a new scan; >0 loads an existing receipt. */
    @Serializable
    data class ReceiptScanning(val id: Long) : PageNavigation

    @Serializable
    data object ShoppingList : PageNavigation

    @Serializable
    data class ShoppingListUndermenu(val listID: String) : PageNavigation

    @Serializable
    data object Database : PageNavigation

    /** [month] is 1-12, [year] the four-digit year. */
    @Serializable
    data class Budget(val year: Int, val month: Int) : PageNavigation

    @Serializable
    data object Settings : PageNavigation
}
