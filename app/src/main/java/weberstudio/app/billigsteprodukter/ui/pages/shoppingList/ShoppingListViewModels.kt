package weberstudio.app.billigsteprodukter.ui.pages.shoppingList

import android.app.Application
import android.util.Log
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
import kotlinx.coroutines.flow.combine
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
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptRepository
import weberstudio.app.billigsteprodukter.logic.MatchScoreCalculator
import weberstudio.app.billigsteprodukter.logic.Store
import java.time.LocalDate

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
    private val _databaseSearchQuery = MutableStateFlow("")
    private val _listSearchQuery = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    private val _isStoreExpanded = MutableStateFlow<Map<Int, Boolean>>(emptyMap())

    val databaseSearchQuery = _databaseSearchQuery.asStateFlow()
    val listSearchQuery = _listSearchQuery.asStateFlow()
    val searchResults = _searchResults.asStateFlow()
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

    val store2ProductsAdded2Store = _selectedShoppingListID.flatMapLatest { listID ->
        if (listID != null) shoppingListRepo.getShoppingListProductsGroupedByStore(listID)
        else flowOf(emptyMap())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyMap()
    )

    val storeTotals = _selectedShoppingListID.flatMapLatest { listID ->
        if (listID != null) shoppingListRepo.getStoreTotals(listID)
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

    @OptIn(FlowPreview::class)
    val filteredStore2ProductsAdded2Store = run {
        //region SETTINGS
        val minimumQueryLength = 3 //How long the query has to be before search kicks in
        val inputCooldown: Long = 300 //How long in MS from the user stops inputting 'till query starts
        val itemLimit = 30 //How many items to retrieve per store
        //endregion

        store2ProductsAdded2Store.combine(
            listSearchQuery
                .debounce(inputCooldown)
                .distinctUntilChanged()
        ) { store2Products, rawQuery ->
            val query = rawQuery.trim().lowercase()
            when {
                query.isBlank() || query.length < minimumQueryLength -> {
                    store2Products
                }
                else -> {
                    store2Products.mapValues { (store, productPair) ->
                        productPair.map { productPair ->
                            val score = MatchScoreCalculator.calculate(productPair.first.name, query)
                            Triple(productPair.first, productPair.second, score)
                        }
                        .filter { it.third > 0 } // Only keep products with match score > 0
                        .sortedByDescending { it.third } // Sort by match score
                        .map { Pair(it.first, it.second) } // Convert back to Pair<Product, Boolean>
                        .take(itemLimit)
                    }
                }
            }.filterValues { products ->
                products.isNotEmpty()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )
    }

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
            currentMap + (store.ID to (currentMap[store.ID] != true))
        }
    }

    fun setDatabaseSearchQuery(query: String) { _databaseSearchQuery.value = query }
    fun setListSearchQuery(query: String) { _listSearchQuery.value = query }

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

    private var nextNegativeID = -1L
    /**
     * Adds a product only scoped to this list and will not be saved beyond this shoppingList
     */
    fun addCustomProductToList(name: String, store: Store) {
        viewModelScope.launch {
            _selectedShoppingListID.value?.let { listID ->
                val customProduct = Product(
                    databaseID = nextNegativeID--,
                    name = name,
                    price = 0f,
                    store = store
                )

                val productID = productRepo.addProduct(customProduct)
                Log.d("ShoppingList", "ProductID: $productID")

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
     * Searches for products in the product database
     */
    fun searchProductsInDatabase(query: String) {
        _databaseSearchQuery.value = query
        if (query.length >= 3) {
            viewModelScope.launch {
                productRepo.searchProductsContaining(query).collect { products ->
                    _searchResults.value = products
                }
            }
        } else {
            _searchResults.value = emptyList()
        }
    }
}

