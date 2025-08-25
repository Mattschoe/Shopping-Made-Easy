package weberstudio.app.billigsteprodukter

import android.app.Application
import weberstudio.app.billigsteprodukter.data.budget.BudgetDatabase
import weberstudio.app.billigsteprodukter.data.budget.OfflineBudgetRepository
import weberstudio.app.billigsteprodukter.data.receipt.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptDatabase

/**
 * A application that runs beside the program to access repo's/databases
 */
class ReceiptApp : Application() {
    val receiptRepository by lazy {
        val database = ReceiptDatabase.getDatabase(this)
        OfflineReceiptRepository(database.receiptDao())
    }

    val budgetRepository by lazy {
        val database = BudgetDatabase.getDatabase(this)
        OfflineBudgetRepository(database.budgetDao())
    }
}