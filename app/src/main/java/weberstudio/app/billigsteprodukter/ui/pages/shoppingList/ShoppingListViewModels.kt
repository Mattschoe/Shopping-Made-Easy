package weberstudio.app.billigsteprodukter.ui.pages.shoppingList

import android.app.Application
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.shoppingList.ShoppingListRepository
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.data.ShoppingListWithProducts
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptRepository
import weberstudio.app.billigsteprodukter.logic.Store
import java.time.LocalDate
import java.time.LocalDateTime

class ShoppingListsViewModel(application: Application): AndroidViewModel(application) {
    private val app = application as ReceiptApp
    private val shoppingListRepo: ShoppingListRepository = app.shoppingListRepository

    val shoppingLists = shoppingListRepo.getAllShoppingLists().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )



    fun addShoppingList(name: String) {
        val nextNumber = shoppingLists.value.size + 1
        val shoppingList = ShoppingList(
            ID = "shoppingList_$nextNumber",
            name = if (name.isBlank()) "Min indkøbsliste $nextNumber" else name,
            createdDate = LocalDate.now()
        )

        viewModelScope.launch {
            shoppingListRepo.insert(shoppingList)
        }
    }
}

class ShoppingListUndermenuViewModel(application: Application): AndroidViewModel(application) {
    private val app = application as ReceiptApp
    private val shoppingListRepo: ShoppingListRepository = app.shoppingListRepository
    private val productRepo: ReceiptRepository = app.receiptRepository

    private val _selectedShoppingListID = MutableStateFlow<String?>(null)
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedShoppingList = _selectedShoppingListID.flatMapLatest { ID ->
        if (ID == null) flowOf(null)
        else shoppingListRepo.getShoppingListWithProducts(ID)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )



    fun selectShoppingList(shoppingListID: String) {
        _selectedShoppingListID.value = shoppingListID
    }

    fun addProduct(name: String, store: Store) {
        TODO("Sut pik")
    }

    /**
     * Removes the product given from the productID from the current selectedShoppingList
     */
    fun removeProduct(productID: Long) {
        val shoppingListID = _selectedShoppingListID.value ?: return
        viewModelScope.launch {
            shoppingListRepo.removeProductFromShoppingList(shoppingListID, productID)
        }
    }
}

