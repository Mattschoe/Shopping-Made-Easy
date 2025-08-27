package weberstudio.app.billigsteprodukter.data.budget

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.ExtraExpense
import java.time.Month
import java.time.Year

@Dao
interface BudgetDao {
    @Insert
    suspend fun insertBudget(budget: Budget): Long

    @Insert
    suspend fun insertExtraExpense(expense: ExtraExpense): Long

    @Delete
    suspend fun deleteBudget(budget: Budget)

    //region QUERIES
    @Query("DELETE FROM budgets WHERE month = :month AND year = :year")
    suspend fun deleteBudgetByDate(month: Month, year: Year)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun getBudgetByDate(month: Month, year: Year): Flow<Budget?>

    @Query("SELECT * FROM extra_expenses WHERE month = :month AND year = :year ORDER BY DATE")
    fun getExtraExpenses(month: Month, year: Year): Flow<List<ExtraExpense>>
    //endregion
}