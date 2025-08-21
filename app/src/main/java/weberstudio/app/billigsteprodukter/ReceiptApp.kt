package weberstudio.app.billigsteprodukter

import android.app.Application
import weberstudio.app.billigsteprodukter.data.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.data.ReceiptDatabase

/**
 * A application that runs beside the program to access repo's/databases
 */
class ReceiptApp : Application() {
    val receiptRepository by lazy {
        val database = ReceiptDatabase.getDatabase(this)
        OfflineReceiptRepository(database.receiptDao())
    }
}