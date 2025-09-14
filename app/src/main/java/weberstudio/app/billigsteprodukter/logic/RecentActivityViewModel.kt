package weberstudio.app.billigsteprodukter.logic

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.ActivityType
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.Receipt
import weberstudio.app.billigsteprodukter.data.RecentActivity
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.data.recentactivity.OfflineActivityRepository

class RecentActivityViewModel(application: Application): AndroidViewModel(application) {
    private val activityRepo: OfflineActivityRepository = (application as ReceiptApp).activityRepository

    private val _recentActivities = MutableStateFlow(activityRepo.getRecentActivities())
    val recentActivities = _recentActivities.asStateFlow()
    
    suspend fun logReceiptScan(receipt: Receipt) {
        val activity = RecentActivity(
            activityType = ActivityType.RECEIPT_SCANNED,
            displayInfo = "Scannede ${receipt.store.name} kvittering fra d. ${receipt.date}",
            receiptID = receipt.receiptID
        )
        activityRepo.insertActivity(activity)
    }

    suspend fun logBudgetCreated(budget: Budget) {
        val activity = RecentActivity(
            activityType = ActivityType.BUDGET_CREATED,
            displayInfo = "Lavede budget på: ${budget.budget} for ${budget.month} måned",
            budgetMonth = budget.month,
            budgetYear = budget.year
        )
        activityRepo.insertActivity(activity)
    }

    suspend fun logShoppingListCreated(shoppingList: ShoppingList) {
        val activity = RecentActivity(
            activityType = ActivityType.SHOPPING_LIST_CREATED,
            displayInfo = "",
            shoppingListID = shoppingList.ID
        )
        activityRepo.insertActivity(activity)
    }
}