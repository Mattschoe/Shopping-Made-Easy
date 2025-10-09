package weberstudio.app.billigsteprodukter.ui

import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Store

/**
 * Displays the state of the image parsing
 */
sealed class ParsingState {
    object NotActivated : ParsingState()
    object Idle : ParsingState()
    object InProgress : ParsingState()
    data class Success(val parsedStore: Store) : ParsingState()
    data class Error(val message: String) : ParsingState()
}

sealed class ReceiptUIState {
    object Loading : ReceiptUIState()
    data class Success(
        val products: List<Product>,
        val store: Store
    ) : ReceiptUIState()
    object Empty : ReceiptUIState()
}