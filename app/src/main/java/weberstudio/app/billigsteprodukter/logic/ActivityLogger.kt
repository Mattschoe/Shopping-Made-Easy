package weberstudio.app.billigsteprodukter.logic

import weberstudio.app.billigsteprodukter.data.ActivityType
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.Receipt
import weberstudio.app.billigsteprodukter.data.RecentActivity
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.data.recentactivity.ActivityRepository
import weberstudio.app.billigsteprodukter.logic.Formatter.filterInputToValidNumberInput
import weberstudio.app.billigsteprodukter.logic.Formatter.formatFloatToDanishCurrency
import weberstudio.app.billigsteprodukter.logic.Formatter.formatInputToDanishCurrency
import weberstudio.app.billigsteprodukter.logic.Formatter.toDanishString
import java.time.format.DateTimeFormatter

class ActivityLogger(private val activityRepo: ActivityRepository) {
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
            displayInfo = "Lavede budget på: ${formatFloatToDanishCurrency(budget.budget)}kr for ${budget.month.toDanishString()} måned",
            budgetMonth = budget.month,
            budgetYear = budget.year
        )

        activityRepo.insertActivity(activity)
    }

    suspend fun logShoppingListCreated(shoppingList: ShoppingList) {
        val formatter = DateTimeFormatter.ofPattern("dd/MM")
        val activity = RecentActivity(
            activityType = ActivityType.SHOPPING_LIST_CREATED,
            displayInfo = "Oprettede indkøbsliste: '${shoppingList.name}' d. ${formatter.format(shoppingList.createdDate)}",
            shoppingListID = shoppingList.ID
        )
        activityRepo.insertActivity(activity)
    }
}