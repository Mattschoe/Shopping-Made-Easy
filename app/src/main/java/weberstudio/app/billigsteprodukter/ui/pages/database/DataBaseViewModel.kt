package weberstudio.app.billigsteprodukter.ui.pages.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.isEqualTo
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptRepository
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.components.MatchScoreCalculator

/**
 * ViewModel for pulling information to showcase the UI database
 */
class DataBaseViewModel(application: Application): AndroidViewModel(application) {
    private val _currentSelectedStore = MutableStateFlow<Store>(Store.Netto)
    private val _searchQuery = MutableStateFlow("")
    private val _searchAllStores = MutableStateFlow(false)

    private val receiptRepo: ReceiptRepository = (application as ReceiptApp).receiptRepository

    val searchQuery = _searchQuery.asStateFlow()
    val searchAllStores = _searchAllStores.asStateFlow()
    val currentStore = _currentSelectedStore.asStateFlow()
    val allStoresSearchEnabled = _searchAllStores.asStateFlow()

    /**
     * Filters products, only returning those that are not equal to another.
     * Keeps the conservative fuzzy name match (to merge OCR variants), but uses an effectively-exact
     * price epsilon so genuinely different products are no longer collapsed.
     */
    private fun filterProducts(products: List<Product>): List<Product> {
        val results = mutableListOf<Product>()
        for (product in products) {
            if (results.none { it.isEqualTo(product, useFuzzyMatcher = true) }) results.add(product)
        }
        return results
    }

    /**
     * Filters, ranks and returns the products based on how much they match the searchQuery, and returns a list of items matching the query + parameters given.
     *
     * Observes the database directly (via Room [kotlinx.coroutines.flow.Flow]s) so newly scanned
     * products show up live without any manual cache.
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val filteredProductsFlow: StateFlow<List<Product>> by lazy {
        //region SETTINGS
        val minimumQueryLength = 2 //How long the query has to be before search kicks in
        val inputCooldown: Long = 300 //How long in MS from the user stops inputting 'till query starts
        val itemLimit = 30 //How many items to retrieve from database
        //endregion

        combine(
            _searchAllStores,
            _searchQuery
                .debounce(inputCooldown)
                .distinctUntilChanged(),
            _currentSelectedStore
        ) { useAll, rawQuery, currentStore ->
            Triple(useAll, rawQuery.trim(), currentStore)
        }
        .flatMapLatest { (useAll, trimmedQuery, currentStore) ->
            val query = trimmedQuery.lowercase()
            val hasQuery = query.length >= minimumQueryLength

            //Picks the live source depending on scope (all stores vs. current) and whether a query is active
            val source = when {
                useAll && hasQuery -> receiptRepo.searchProductsContaining(query)
                useAll -> receiptRepo.getAllProducts()
                hasQuery -> receiptRepo.searchProductsByStoreContaining(currentStore, query)
                else -> receiptRepo.getProductsByStore(currentStore)
            }

            source.map { products ->
                //Ranks the product so they are in correct order. For more see: calculateMatchScore()
                val sortedProducts = if (!hasQuery) {
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
        }
        .map { filterProducts(it) }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    }

    /**
     * Toggles the product given as argument as favorite. Acts on the single row (databaseID) so
     * separate cards (e.g. the same product at two different prices) toggle independently.
     */
    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            receiptRepo.setProductFavorite(product.databaseID, !product.isFavorite)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            //Deletes the single row (databaseID) so separate cards delete independently.
            //The repo recomputes affected receipt totals (a product can be linked to multiple receipts).
            receiptRepo.deleteProduct(product)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            receiptRepo.updateProduct(product)
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
