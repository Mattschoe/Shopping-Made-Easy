package weberstudio.app.billigsteprodukter.logic

import android.content.Context
import weberstudio.app.billigsteprodukter.data.ActivityType
import weberstudio.app.billigsteprodukter.data.AppDatabase
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.Receipt
import weberstudio.app.billigsteprodukter.data.RecentActivity
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.data.recentactivity.RecentActivityDao

object ActivityLogger {
    private lateinit var dao: RecentActivityDao

    fun init(context: Context) {
        val database = AppDatabase.getDatabase(context)
        dao = database.recentActivityDao()
    }

    suspend fun logReceiptScan(receipt: Receipt) {
        val activity = RecentActivity(
            activityType = ActivityType.RECEIPT_SCANNED,
            displayInfo = "Scannede ${receipt.store.name} kvittering fra d. ${receipt.date}",
            receiptID = receipt.receiptID
        )
        dao.insertActivity(activity)
    }

    suspend fun logBudgetCreated(budget: Budget) {
        val activity = RecentActivity(
            activityType = ActivityType.BUDGET_CREATED,
            displayInfo = "Lavede budget på: ${budget.budget} for ${budget.month} måned",
            budgetMonth = budget.month,
            budgetYear = budget.year
        )
        dao.insertActivity(activity)
    }

    suspend fun logShoppingListCreated(shoppingList: ShoppingList) {
        val activity = RecentActivity(
            activityType = ActivityType.SHOPPING_LIST_CREATED,
            displayInfo = "",
            shoppingListID = shoppingList.ID
        )
        dao.insertActivity(activity)
    }
}