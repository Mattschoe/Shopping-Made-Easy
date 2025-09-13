package weberstudio.app.billigsteprodukter

import android.app.Application
import weberstudio.app.billigsteprodukter.data.AppDatabase
import weberstudio.app.billigsteprodukter.data.budget.OfflineBudgetRepository
import weberstudio.app.billigsteprodukter.data.receipt.OfflineReceiptRepository

/**
 * A application that runs beside the program to access repo's/databases
 */
class ReceiptApp : Application() {
    val receiptRepository by lazy {
        val database = AppDatabase.getDatabase(this)
        OfflineReceiptRepository(database.receiptDao())
    }

    val budgetRepository by lazy {
        val database = AppDatabase.getDatabase(this)
        OfflineBudgetRepository(database.budgetDao())
    }
}