package weberstudio.app.billigsteprodukter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import weberstudio.app.billigsteprodukter.data.budget.BudgetDao
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptDao
import weberstudio.app.billigsteprodukter.data.recentactivity.RecentActivityDao

@Database(
    entities = [Receipt::class, Product::class, RecentActivity::class, Budget::class, ExtraExpense::class, ShoppingList::class, ShoppingListCrossRef::class],
    version = 4,
    exportSchema =  false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recentActivityDao(): RecentActivityDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}