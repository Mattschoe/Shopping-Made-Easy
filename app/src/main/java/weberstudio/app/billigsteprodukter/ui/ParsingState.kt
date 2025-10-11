package weberstudio.app.billigsteprodukter.ui

import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Store

/**
 * Displays the state of the image parsing
 */
sealed class ParsingState {
    data object NotActivated : ParsingState()
    data object Idle : ParsingState()
    data object InProgress : ParsingState()
    data class Success(val store: Store, val receiptID: Long) : ParsingState()
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