package weberstudio.app.billigsteprodukter.ui.pages.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance
import weberstudio.app.billigsteprodukter.data.OfflineReceiptRepository
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Store

/**
 * ViewModel for pulling information to showcase the UI database
 */
class DataBaseViewModel(): ViewModel() {
    private val _currentSelectedStore = MutableStateFlow<Store>(Store.Netto)
    private val _searchQuery = MutableStateFlow("")
    private val _searchAllStores = MutableStateFlow(false)

    private val productRepo: OfflineReceiptRepository = OfflineReceiptRepository
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
                productRepo.getProductsByStore(store)
            }
            //
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000), //Waits 5 seconds after all collectors go away
                initialValue = emptyList()
            )
    }

    /**
     * Filters, ranks and returns the products based on how much they match the searchQuery, and returns a list of items matching the query + parameters given
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun filteredProductsFlow(): StateFlow<List<Product>> {
        //Retrieves either all products or just from the currentStore depending on _searchAllStores
        val baseFlow: Flow<List<Product>> =
            _searchAllStores
                .flatMapLatest { useAll ->
                    if (useAll) productRepo.productStream
                    else getProductsFromCurrentStore()
                }
        return baseFlow
            .combine(_searchQuery) { products, rawQuery ->
                val query = rawQuery.trim().lowercase()
                if (query.isBlank()) return@combine products

                //Sorterer og ranker produkterne
                products
                    .map { it to calculateMatchScore(it.name, query) }
                    .filter { it.second > 0 }
                    .sortedByDescending { it.second }
                    .map { it.first }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    }

    /**
     * Toggles the product given as argument as favorite
     */
    fun toggleFavorite(product: Product) {
        val toggledFavoriteStatus = !product.isFavorite
        viewModelScope.launch {
            productRepo.setProductFavorite(product.businessID, toggledFavoriteStatus)
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