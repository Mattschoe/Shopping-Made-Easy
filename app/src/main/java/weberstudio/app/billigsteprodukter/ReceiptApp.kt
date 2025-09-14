package weberstudio.app.billigsteprodukter

import android.app.Application
import weberstudio.app.billigsteprodukter.data.AppDatabase
import weberstudio.app.billigsteprodukter.data.budget.OfflineBudgetRepository
import weberstudio.app.billigsteprodukter.data.receipt.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.data.recentactivity.OfflineActivityRepository
import weberstudio.app.billigsteprodukter.data.shoppingList.OfflineShoppingListRepository

/**
 * A application that runs beside the program to access repo's/databases
 */
class ReceiptApp : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }

    val activityRepository by lazy { OfflineActivityRepository(database.recentActivityDao()) }
    val receiptRepository by lazy { OfflineReceiptRepository(database.receiptDao()) }
    val budgetRepository by lazy { OfflineBudgetRepository(database.budgetDao()) }
    val shoppingListRepository by lazy { OfflineShoppingListRepository(database.ShoppingListDao()) }
}