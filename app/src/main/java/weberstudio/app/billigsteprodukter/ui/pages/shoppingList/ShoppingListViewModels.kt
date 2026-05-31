package weberstudio.app.billigsteprodukter.ui.pages.shoppingList

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.shoppingList.ShoppingListRepository
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.data.ShoppingListCrossRef
import weberstudio.app.billigsteprodukter.data.isEqualTo
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptRepository
import weberstudio.app.billigsteprodukter.logic.Store
import java.time.LocalDate
import kotlin.math.cos
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ShoppingListsViewModel(application: Application): AndroidViewModel(application) {
    private val app = application as ReceiptApp
    private val shoppingListRepo: ShoppingListRepository = app.shoppingListRepository

    val shoppingLists = shoppingListRepo.getAllShoppingLists().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )


    @OptIn(ExperimentalUuidApi::class)
    fun addShoppingList(name: String) {
        val ID = Uuid.random()
        val nextNumber = shoppingLists.value.size + 1
        val shoppingList = ShoppingList(
            ID = "shoppingList_$ID",
            name = if (name.isBlank()) "Min indkøbsliste $nextNumber" else name,
            createdDate = LocalDate.now()
        )

        viewModelScope.launch {
            shoppingListRepo.insert(shoppingList)
            app.activityLogger.logShoppingListCreated(shoppingList)
        }
    }

    fun deleteShoppingList(list: ShoppingList) {
        viewModelScope.launch {
            shoppingListRepo.deleteShoppingList(list.ID)
        }
    }
}

class ShoppingListUndermenuViewModel(application: Application): AndroidViewModel(application) {
    private val app = application as ReceiptApp
    private val shoppingListRepo: ShoppingListRepository = app.shoppingListRepository
    private val productRepo: ReceiptRepository = app.receiptRepository

    private val _selectedShoppingListID = MutableStateFlow<String?>(null)
    private val _databaseSearchQuery = MutableStateFlow("")
    private val _isStoreExpanded = MutableStateFlow<Map<Store, Boolean>>(emptyMap())

    val isStoreExpanded = _isStoreExpanded.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedShoppingList = _selectedShoppingListID.flatMapLatest { ID ->
        if (ID == null) flowOf(null)
        else shoppingListRepo.getShoppingListWithProducts(ID)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val store2ProductsAdded2Store = _selectedShoppingListID.flatMapLatest { listID ->
        if (listID != null) shoppingListRepo.getShoppingListProductsGroupedByStore(listID)
        else flowOf(emptyMap())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    val priceTotal: StateFlow<Double> = selectedShoppingList.map { shoppingListWithProduct ->
        shoppingListWithProduct?.products?.sumOf { it.price.toDouble() } ?: 0.0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0.0
    )

    /**
     * Søgeresultater til "Tilføj produkt"-dialogen. Afledt reaktivt af [_databaseSearchQuery] så hver
     * ny søgning annullerer den forrige collector (flatMapLatest) — ingen lækkede flows eller
     * akkumulerede resultater på tværs af emissions.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<Product>> = _databaseSearchQuery
        .debounce(300) //Hvor lang tid i MS fra brugeren stopper med at skrive 'til søgningen starter
        .distinctUntilChanged()
        .flatMapLatest { rawQuery ->
            val query = rawQuery.trim()
            if (query.length < 3) flowOf(emptyList())
            else productRepo.searchProductsContaining(query).map { products ->
                //Dedupliker friskt pr. emission, så fjernede/ændrede produkter ikke hænger ved
                val deduped = products.fold(mutableListOf<Product>()) { acc, product ->
                    if (acc.none { it.isEqualTo(product, priceDifferenceEpsilon = 5.0f, useFuzzyMatcher = true) }) {
                        acc.add(product)
                    }
                    acc
                }
                deduped
                    .filter { it.price > 0.2f }
                    .sortedWith(
                        compareByDescending<Product> { it.isFavorite }
                            .thenBy { it.price }
                    )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /**
     * Toggles the product to/from selected
     */
    fun toggleProduct(product: Product) {
        viewModelScope.launch {
            _selectedShoppingListID.value?.let { listID ->
                shoppingListRepo.toggleProductChecked(listID, product.databaseID)
            }
        }
    }

    /**
     * Toggles if the store is expanded into a dropdown
     */
    fun toggleStore(store: Store) {
        _isStoreExpanded.update { currentMap ->
            currentMap + (store to !currentMap.isExpanded(store))
        }
    }

    fun selectShoppingList(shoppingListID: String) {
        _selectedShoppingListID.value = shoppingListID
    }

    fun addExistingProductToList(product: Product) {
        viewModelScope.launch {
            _selectedShoppingListID.value?.let { listID ->

                if (!shoppingListRepo.isProductInShoppingList(listID, product.databaseID)) {
                    val crossRef = ShoppingListCrossRef(
                        shoppingListID = listID,
                        productID = product.databaseID,
                        isChecked = false
                    )
                    shoppingListRepo.insertShoppingListProductCrossRef(crossRef)

                }
            }
        }
    }

    /**
     * Adds a product only scoped to this list and will not be saved beyond this shoppingList
     */
    fun addCustomProductToList(name: String, store: Store) {
        viewModelScope.launch {
            _selectedShoppingListID.value?.let { listID ->
                var productID = shoppingListRepo.getLastNegativeID() - 1
                val customProduct = Product(
                    databaseID = productID,
                    name = name,
                    price = 0f,
                    store = store
                )
                productID = productRepo.addProduct(customProduct)

                val crossRef = ShoppingListCrossRef(
                    shoppingListID = listID,
                    productID = productID,
                    isChecked = false
                )
                shoppingListRepo.insertShoppingListProductCrossRef(crossRef)
            }
        }
    }

    /**
     * Opdaterer søgningen i produktdatabasen. Resultaterne afledes reaktivt i [searchResults].
     */
    fun searchProductsInDatabase(query: String) {
        _databaseSearchQuery.value = query
    }

    fun updateShoppingListName(name: String) {
        viewModelScope.launch {
            _selectedShoppingListID.value?.let { listID ->
                val currentShoppingList = selectedShoppingList.value
                currentShoppingList?.let { list ->
                    val updatedList = list.shoppingList.copy(name = name)
                    shoppingListRepo.updateShoppingList(updatedList)

                }
            }
        }
    }
}

/**
 * Slår op om en butik er foldet ud i indkøbslisten. Stores starter udfoldet, så en manglende
 * nøgle betyder "udfoldet". Defineret ét sted, så ViewModel og UI deler samme standard.
 */
internal fun Map<Store, Boolean>.isExpanded(store: Store): Boolean = this[store] ?: true

