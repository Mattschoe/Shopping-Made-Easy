package weberstudio.app.billigsteprodukter.data.budget

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.Budget
import java.time.Month
import java.time.Year

interface BudgetRepository {
    /**
     * Returns the [Budget] given the month and year.
     */
    suspend fun getBudget(month: Month, year: Year): Flow<Budget?>
}