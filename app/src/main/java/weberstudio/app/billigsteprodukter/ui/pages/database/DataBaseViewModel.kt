package weberstudio.app.billigsteprodukter.ui.pages.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptRepository
import weberstudio.app.billigsteprodukter.logic.Store

/**
 * ViewModel for pulling information to showcase the UI database
 */
class DataBaseViewModel(application: Application): AndroidViewModel(application) {
    private val _currentSelectedStore = MutableStateFlow<Store>(Store.Netto)
    private val _searchQuery = MutableStateFlow("")
    private val _searchAllStores = MutableStateFlow(false)

    private val receiptRepo: ReceiptRepository = (application as ReceiptApp).receiptRepository
    private val fuzzyMatcherLeven = LevenshteinDistance()
    private val fuzzyMatcherJaro = JaroWinklerSimilarity()

    val searchQuery = _searchQuery.asStateFlow()
    val searchAllStores = _searchAllStores.asStateFlow()

    /**
     * The current store selected by user
     */
    val currentStore = _currentSelectedStore.asStateFlow()
    val allStoresSearchEnabled = _searchAllStores.asStateFlow()

    /**
     * Retrieves products from the current store selected by user in the UI
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getProductsFromCurrentStore(): StateFlow<List<Product>> {
        return _currentSelectedStore
            //Cancels last product flow, and collects a new one
            .flatMapLatest { store ->
                receiptRepo.getProductsByStore(store)
            }
            //
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Companion.WhileSubscribed(5_000), //Waits 5 seconds after all collectors go away
                initialValue = emptyList()
            )
    }

    /**
     * Filters, ranks and returns the products based on how much they match the searchQuery, and returns a list of items matching the query + parameters given
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun filteredProductsFlow(): StateFlow<List<Product>> {
        //region SETTINGS
        val minimumQueryLength = 2 //How long the query has to be before search kicks in
        val inputCooldown: Long = 300 //How long in MS from the user stops inputting 'till query starts
        val itemLimit = 30 //How many items to retrieve from database
        //endregion

        //Retrieves either all products or just from the currentStore depending on _searchAllStores
        return searchAllStores
            .combine(
                _searchQuery
                    .debounce(inputCooldown)
                    .distinctUntilChanged()
            ) { useAll, rawQuery ->
                val query = rawQuery.trim().lowercase()

                when {
                    query.isBlank() || query.length < minimumQueryLength -> {
                        // Show all products from current store when no search query
                        if (useAll) receiptRepo.searchProductsContaining(query) // You might need to add this method
                        else receiptRepo.getProductsByStore(currentStore.value)
                    }
                    useAll -> receiptRepo.searchProductsContaining(query)
                    else -> receiptRepo.searchProductsByStoreContaining(currentStore.value, query)
                }
            }
            .flatMapLatest { flow -> flow }
            .map { products ->
                //Ranks the product so they are in correct order. For more see: calculateMatchScore()
                val query = _searchQuery.value.trim().lowercase()

                // If no search query, return products as-is (maybe with a limit)
                if (query.isBlank() || query.length < minimumQueryLength) {
                    return@map products.take(itemLimit)
                }

                // Otherwise, rank and filter by search relevance
                products
                    .map { it to calculateMatchScore(it.name, query) }
                    .filter { it.second > 0 }
                    .sortedByDescending { it.second }
                    .map { it.first }
                    .take(itemLimit)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Companion.WhileSubscribed(5_000),
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
        }
    }

    private fun calculateMatchScore(productName: String, query: String): Int {
        //TODO: Det her skal normalizes helt (se hvordan i parser), og ikke bar trimmes og lowercases
        val productName = productName.lowercase().trim()
        val query = query.lowercase().trim()

        return when {
            productName == query -> 100
            productName.startsWith(query) -> 75
            productName.contains(query) -> 50
            fuzzyMatch(productName, query) -> 25
            else -> 0
        }
    }

    /**
     * Fuzzy matches the productName and query
     */
    private fun fuzzyMatch(productName: String, query: String): Boolean {
        val jaroSimilarity = fuzzyMatcherJaro.apply(productName, query)
        if (jaroSimilarity >= 0.85) return true

        val levenSimilarity = fuzzyMatcherLeven.apply(productName, query)
        return levenSimilarity != -1 && levenSimilarity <= 2
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