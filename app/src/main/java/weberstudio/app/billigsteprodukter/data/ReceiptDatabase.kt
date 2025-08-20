package weberstudio.app.billigsteprodukter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Receipt::class, Product::class], version = 1, exportSchema =  false)
@TypeConverters(Converters::class)
abstract class ReceiptDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao

    companion object {
        @Volatile private var INSTANCE: ReceiptDatabase? = null

        fun getDatabase(context: Context): ReceiptDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ReceiptDatabase::class.java,
                    "receipt_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}