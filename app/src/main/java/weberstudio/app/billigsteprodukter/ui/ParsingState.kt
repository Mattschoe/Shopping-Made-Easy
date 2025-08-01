package weberstudio.app.billigsteprodukter.ui

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