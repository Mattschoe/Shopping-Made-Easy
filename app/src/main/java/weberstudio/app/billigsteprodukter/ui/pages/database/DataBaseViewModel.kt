package weberstudio.app.billigsteprodukter.ui.pages.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import weberstudio.app.billigsteprodukter.data.ReceiptRepository
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.logic.Store

/**
 * ViewModel for pulling information to showcase the UI database
 */
class DataBaseViewModel(): ViewModel() {
    private val productRepo: ReceiptRepository = ReceiptRepository
    private val _currentSelectedStore = MutableStateFlow<Store>(Store.Netto)

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
     * Changes the state of which store the user is looking at
     */
    fun selectStore(store: Store) {
        _currentSelectedStore.value = store
    }
}