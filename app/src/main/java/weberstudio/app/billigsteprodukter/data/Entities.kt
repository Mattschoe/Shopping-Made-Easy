package weberstudio.app.billigsteprodukter.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import kotlinx.parcelize.Parcelize
import weberstudio.app.billigsteprodukter.logic.Store

/**
 * A product in a store.
 * @param receiptID the ID of the inserted receipt that the product belong sto
 * @param businessID for handling business logic
 */
@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["receiptID"],
            childColumns = ["receiptID"],
            onDelete = ForeignKey.SET_NULL //Så vi ikke mister produkterne hvis kvitteringerne bliver slettet
        )
    ],
    indices = [Index(value = ["receiptID"])] //Faster lookup fra kvittering -> produkter i kvitteringen
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val databaseID: Long = 0, //Database logic ID
    val receiptID: Long?,
    val name: String,
    val price: Float,
    val store: Store,
    var isFavorite: Boolean = false
    ) {
    val businessID: ProductID //Business logic ID
        get() = ProductID(store, name)
}

/**
 * Unique identifier for "one product in one store"
 */
@Parcelize
data class ProductID(
    val store: Store,
    val name: String
): Parcelable

@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey(autoGenerate = true)
    val receiptID: Long = 0,
    val store: Store,
    val date: Long,
    val total: Float
)

/**
 * Represents a *Receipt containing products*. Is a helper class for easily reading/querying both the receipt class and the products inside that receipt in a singular database call. It is **NOT** meant for creating data
 */
data class ReceiptWithProducts(
    @Embedded val receipt: Receipt,
    @Relation(
        parentColumn = "receiptID",
        entityColumn = "receiptID"
    )
    val products: List<Product>
)

