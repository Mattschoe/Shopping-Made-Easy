package weberstudio.app.billigsteprodukter.ui.pages.database

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.isEqualTo
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptRepository
import weberstudio.app.billigsteprodukter.logic.components.MatchScoreCalculator
import weberstudio.app.billigsteprodukter.logic.Store
import kotlin.math.abs

/**
 * ViewModel for pulling information to showcase the UI database
 */
class DataBaseViewModel(application: Application): AndroidViewModel(application) {
    private val _currentSelectedStore = MutableStateFlow<Store>(Store.Netto)
    private val _searchQuery = MutableStateFlow("")
    private val _searchAllStores = MutableStateFlow(false)

    private val _storeProductsCache = MutableStateFlow<Map<Store, List<Product>>>(emptyMap())
    private val _loadingStores = MutableStateFlow<Set<Store>>(emptySet())

    private val receiptRepo: ReceiptRepository = (application as ReceiptApp).receiptRepository

    val searchQuery = _searchQuery.asStateFlow()
    val searchAllStores = _searchAllStores.asStateFlow()
    val currentStore = _currentSelectedStore.asStateFlow()
    val allStoresSearchEnabled = _searchAllStores.asStateFlow()

    //Preloads stores on init
    init {
        viewModelScope.launch {
            preloadAdjacentStores(_currentSelectedStore.value)
        }

        //When store change we preload the new adjacent ones, and clear the old out of memory
        viewModelScope.launch {
            _currentSelectedStore.collectLatest { newStore ->
                preloadAdjacentStores(newStore)
                cleanupDistantStores(newStore)
            }
        }
    }

    private suspend fun preloadAdjacentStores(currentStore: Store) {
        val stores = Store.entries.toList()
        val currentStoreIndex = stores.indexOf(currentStore)

        val stores2Preload = (-2..2).mapNotNull { offset ->
            val index = currentStoreIndex + offset
            stores.getOrNull(index)
        }

        stores2Preload.forEach { store ->
            if (store !in _storeProductsCache.value.keys && store !in _loadingStores.value) {
                loadStoreProducts(store)
            }
        }
    }

    private suspend fun loadStoreProducts(store: Store) {
        _loadingStores.update { it + store } //Marks store as loading

        try {
            receiptRepo.getProductsByStore(store)
                .take(1)
                .collect { products ->
                    _storeProductsCache.update { cache ->
                        cache + (store to products)
                    }
                }
        } catch (e: Exception) {
            Log.d("Database Page", e.toString())
        } finally {
            _loadingStores.update { it - store }
        }
    }

    private fun cleanupDistantStores(currentStore: Store) {
        val stores = Store.entries.toList()
        val currentStoreIndex = stores.indexOf(currentStore)

        _storeProductsCache.update { cache ->
            cache.filterKeys { cachedStore ->
                val cachedIndex = stores.indexOf(cachedStore)
                abs(cachedIndex - currentStoreIndex) <= 3
            }
        }
    }

    /**
     * Filters products, only returning those that are not equal to another.
     */
    private fun filterProducts(products: List<Product>): List<Product> {
        val results = mutableListOf<Product>()
        for (product in products) {
            if (results.none { it.isEqualTo(product, priceDifferenceEpsilon = 5.0f, useFuzzyMatcher = true) }) results.add(product)
        }
        return results
    }

    /**
     * Filters, ranks and returns the products based on how much they match the searchQuery, and returns a list of items matching the query + parameters given
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val filteredProductsFlow: StateFlow<List<Product>> by lazy {
        //region SETTINGS
        val minimumQueryLength = 2 //How long the query has to be before search kicks in
        val inputCooldown: Long = 300 //How long in MS from the user stops inputting 'till query starts
        val itemLimit = 30 //How many items to retrieve from database
        //endregion

        //Retrieves either all products or just from the currentStore depending on _searchAllStores
        combine(
            searchAllStores,
            _searchQuery
                .debounce(inputCooldown)
                .distinctUntilChanged(),
            _currentSelectedStore,
            _storeProductsCache
        ) { useAll, rawQuery, currentStore, cache ->
            val query = rawQuery.trim().lowercase()

            //Tries to use cache first unless all stores or query is active
            if (useAll || (query.isNotEmpty() && query.length >= minimumQueryLength)) {
                when {
                    query.isBlank() || query.length < minimumQueryLength -> {
                        // Show all products from current store when no search query
                        if (useAll) receiptRepo.searchProductsContaining(query) // You might need to add this method
                        else receiptRepo.getProductsByStore(currentStore)
                    }
                    useAll -> receiptRepo.searchProductsContaining(query)
                    else -> receiptRepo.searchProductsByStoreContaining(currentStore, query)
                }
            } else {
                cache[currentStore]?.let { cachedProducts ->
                    flowOf(cachedProducts)
                } ?: receiptRepo.getProductsByStore(currentStore)
            }
        }
        .flatMapLatest { flow -> flow }
        .map { products ->
            //Ranks the product so they are in correct order. For more see: calculateMatchScore()
            val query = _searchQuery.value.trim().lowercase()

            // If no search query, return products as-is (maybe with a limit)
            val sortedProducts = if (query.isBlank() || query.length < minimumQueryLength) {
                products.take(itemLimit)
            } else {
                //Otherwise, rank and filter by search relevance
                products
                    .map { it to MatchScoreCalculator.calculate(it.name, query) }
                    .filter { it.second > 0 }
                    .sortedByDescending { it.second }
                    .map { it.first }
                    .take(itemLimit)
            }
            sortedProducts.sortedByDescending { it.isFavorite }
        }
        .distinctUntilChanged()
        .map { filterProducts(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )
    }

    /**
     * Toggles the product given as argument as favorite
     */
    fun toggleFavorite(product: Product) {
        val toggledFavoriteStatus = !product.isFavorite
        viewModelScope.launch {
            receiptRepo.setProductFavorite(product.store, product.name, toggledFavoriteStatus)

            //Also updates cache
            _storeProductsCache.update { cache ->
                cache[product.store]?.let { products ->
                    val updatedProducts = products.map {
                        if (it.name == product.name) it.copy(isFavorite = toggledFavoriteStatus)
                        else it
                    }
                    cache + (product.store to updatedProducts)
                } ?: cache
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            receiptRepo.deleteProduct(product)

            //Updates list cache
            _storeProductsCache.update { cache ->
                cache[product.store]?.let { products ->
                    val updated = products.filterNot { it.databaseID == product.databaseID }
                    cache + (product.store to updated)
                } ?: cache
            }

            //Updates receipt total. A bit scuffed since we just update ever receipt from the store, but this is because a product can be linked to multiple receipts.
            receiptRepo.recomputeTotalForReceiptsInStore(product.store)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            receiptRepo.updateProduct(product)

            //Update cache with the modified product
            _storeProductsCache.update { cache ->
                cache[product.store]?.let { products ->
                    val updatedProducts = products.map {
                        if (it.databaseID == product.databaseID) product
                        else it
                    }
                    cache + (product.store to updatedProducts)
                } ?: cache
            }
        }
    }

    /**
     * Changes the state of which store the user is looking at
     */
    fun selectStore(store: Store) {
        _currentSelectedStore.value = store
    }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSearchAllStores(enabled: Boolean) { _searchAllStores.value = enabled }
}