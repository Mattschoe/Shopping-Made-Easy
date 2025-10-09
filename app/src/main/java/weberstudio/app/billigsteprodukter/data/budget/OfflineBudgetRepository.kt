package weberstudio.app.billigsteprodukter.data.budget

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.ExtraExpense
import java.time.Month
import java.time.Year

class OfflineBudgetRepository(private val dao: BudgetDao) : BudgetRepository {
    override suspend fun getBudget(month: Month, year: Year): Flow<Budget?> {
        return dao.getBudgetByDate(month, year)
    }

    override suspend fun insertBudget(budget: Budget) {
        dao.insertBudget(budget)
    }

    override suspend fun getExpenses(month: Month, year: Year): Flow<List<ExtraExpense>> {
        return dao.getExtraExpenses(month, year)
    }

    override suspend fun insertExtraExpense(expense: ExtraExpense) {
        dao.insertExtraExpense(expense)
    }

    override suspend fun deleteExtraExpense(expense: ExtraExpense) {
        dao.deleteExtraExpense(expense)
    }

    override suspend fun updateBudget(budget: Budget) {
        dao.updateBudget(budget)
    }
}