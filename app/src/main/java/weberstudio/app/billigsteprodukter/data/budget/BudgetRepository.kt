package weberstudio.app.billigsteprodukter.data.budget

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.ExtraExpense
import java.time.Month
import java.time.Year

interface BudgetRepository {
    /**
     * Returns the [Budget] given the month and year.
     */
    suspend fun getBudget(month: Month, year: Year): Flow<Budget?>

    /**
     * Inserts a new budget into database
     */
    suspend fun insertBudget(budget: Budget)

    suspend fun getExpenses(month: Month, year: Year): Flow<List<ExtraExpense>>

    suspend fun insertExtraExpense(expense: ExtraExpense)

    suspend fun updateBudget(budget: Budget)
}