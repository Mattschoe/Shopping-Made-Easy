package weberstudio.app.billigsteprodukter.data

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.parcelize.Parcelize
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.logic.Store
import java.time.LocalDate
import java.time.Month
import java.time.Year

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
    val receiptID: Long? = null,
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
    val date: LocalDate,
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

@Entity(
    tableName = "budgets",
    primaryKeys = ["month", "year"]
)
data class Budget(
    val month: Month,
    val year: Year,
    val budget: Float,
)

@Entity(tableName = "extra_expenses")
data class ExtraExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Float,
    val date: LocalDate, // Full date for display
    val month: Month,
    val year: Year
)



@Entity(tableName = "recent_activities",
    foreignKeys = [
        ForeignKey(
            entity = Receipt::class,
            parentColumns = ["receiptID"],
            childColumns = ["receiptID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Budget::class,
            parentColumns = ["month", "year"],
            childColumns = ["budgetMonth", "budgetYear"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["ID"],
            childColumns = ["shoppingListID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["receiptID"]),
        Index(value = ["budgetMonth", "budgetYear"]),
        Index(value = ["shoppingListID"])
    ]
)
data class RecentActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityType: ActivityType,
    val displayInfo: String, //Precomputed for better performance
    val timestamp: Long = System.currentTimeMillis(),

    //Foreign keys (Used for navigation only)
    val receiptID: Long? = null,
    val budgetMonth: Month? = null,
    val budgetYear: Year? = null,
    val shoppingListID: String? = null
)

@Entity(tableName = "shopping_list")
data class ShoppingList(
    @PrimaryKey
    val ID: String,
    val name: String,
    val createdDate: LocalDate
)

/**
 * Junction table since ShoppingList -> Product is a many-to-many relationship
 */
@Entity(
    tableName = "shopping_list_products",
    primaryKeys = ["shoppingListID", "productID"],
    foreignKeys = [
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["ID"],
            childColumns = ["shoppingListID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["databaseID"],
            childColumns = ["productID"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productID"]),
        Index(value = ["shoppingListID"])
    ]
)
data class ShoppingListCrossRef(
    val shoppingListID: String,
    val productID: Long,
    val isChecked: Boolean = false
)

//region HELPER
/**
 * Query helper
 */
data class ShoppingListWithProducts(
    @Embedded val shoppingList: ShoppingList,
    @Relation(
        parentColumn = "ID",
        entityColumn = "databaseID",
        associateBy = Junction(
            ShoppingListCrossRef::class,
            parentColumn = "shoppingListID",
            entityColumn = "productID"
        )
    )
    val products: List<Product>
)

data class ProductWithCheckedStatus(
    @Embedded val product: Product,
    val isChecked: Boolean
)

enum class ActivityType {
    RECEIPT_SCANNED,
    BUDGET_CREATED,
    SHOPPING_LIST_CREATED
}

fun RecentActivity.getIcon(): Int {
    return when (activityType) {
        ActivityType.RECEIPT_SCANNED -> R.drawable.camera_icon
        ActivityType.BUDGET_CREATED -> R.drawable.piggybank_icon
        ActivityType.SHOPPING_LIST_CREATED -> R.drawable.list_icon
    }
}
//endregion



