package weberstudio.app.billigsteprodukter.data.budget

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.Converters

@Database(entities = [Budget::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BudgetDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile private var INSTANCE: BudgetDatabase? = null

        fun getDatabase(context: Context): BudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    BudgetDatabase::class.java,
                    "budget_database"
                )
                    .fallbackToDestructiveMigration(true) //Dropper table når vi skifter version i stedet for at migrate
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

}