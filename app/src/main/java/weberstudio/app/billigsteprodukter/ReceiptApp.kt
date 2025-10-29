package weberstudio.app.billigsteprodukter

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import weberstudio.app.billigsteprodukter.data.AppDatabase
import weberstudio.app.billigsteprodukter.data.budget.OfflineBudgetRepository
import weberstudio.app.billigsteprodukter.data.receipt.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.data.recentactivity.OfflineActivityRepository
import weberstudio.app.billigsteprodukter.data.settings.OfflineSettingsRepository
import weberstudio.app.billigsteprodukter.data.shoppingList.OfflineShoppingListRepository
import weberstudio.app.billigsteprodukter.logic.ActivityLogger

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * A application that runs beside the program to access repo's/databases
 */
class ReceiptApp : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
 
    val activityRepository by lazy { OfflineActivityRepository(database.recentActivityDao()) }
    val receiptRepository by lazy { OfflineReceiptRepository(database.receiptDao()) }
    val budgetRepository by lazy { OfflineBudgetRepository(database.budgetDao()) }
    val shoppingListRepository by lazy { OfflineShoppingListRepository(database.ShoppingListDao()) }
    val settingsRepository by lazy { OfflineSettingsRepository(dataStore, database.receiptDao())  }
    val activityLogger by lazy { ActivityLogger(activityRepository) }
}