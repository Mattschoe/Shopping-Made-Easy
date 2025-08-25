package weberstudio.app.billigsteprodukter.data.budget

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.Budget
import java.time.Month
import java.time.Year

@Dao
interface BudgetDao {
    @Insert
    suspend fun insertBudget(budget: Budget): Long

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("DELETE FROM budgets WHERE month = :month AND year = :year")
    suspend fun deleteBudgetByDate(month: Month, year: Year)

    //region QUERIES
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    suspend fun getBudgetByDate(month: Month, year: Year): Flow<Budget?>


    //endregion
}