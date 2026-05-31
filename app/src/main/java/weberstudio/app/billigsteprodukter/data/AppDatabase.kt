package weberstudio.app.billigsteprodukter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import weberstudio.app.billigsteprodukter.data.budget.BudgetDao
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptDao
import weberstudio.app.billigsteprodukter.data.recentactivity.RecentActivityDao
import weberstudio.app.billigsteprodukter.data.shoppingList.ShoppingListDao

@Database(
    entities = [Receipt::class, Product::class, RecentActivity::class, Budget::class, ExtraExpense::class, ShoppingList::class, ShoppingListCrossRef::class],
    version = 11,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun budgetDao(): BudgetDao
    abstract fun ShoppingListDao(): ShoppingListDao
    abstract fun recentActivityDao(): RecentActivityDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    // Skemaer blev ikke eksporteret før version 11, så ældre databaser
                    // kan ikke migreres og bliver nulstillet én sidste gang. Fra version 11
                    // og frem skal hver versionsbumb ledsages af en rigtig migration.
                    .fallbackToDestructiveMigrationFrom(true, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
                    .build().also { INSTANCE = it }
            }
        }
    }
}