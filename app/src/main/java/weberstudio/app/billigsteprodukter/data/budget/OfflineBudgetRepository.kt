package weberstudio.app.billigsteprodukter.data.budget

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.Budget
import java.time.Month
import java.time.Year

class OfflineBudgetRepository(private val dao: BudgetDao) : BudgetRepository {
    override suspend fun getBudget(month: Month, year: Year): Flow<Budget?> {
        return dao.getBudgetByDate(month, year)
    }

    override suspend fun insertBudget(budget: Budget) {
        dao.insertBudget(budget)
    }
}