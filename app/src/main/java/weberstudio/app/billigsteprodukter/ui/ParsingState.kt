package weberstudio.app.billigsteprodukter.ui

/**
 * Displays the state of the image parsing
 */
sealed class ParsingState {
    object NotActivated : ParsingState()
    object Idle : ParsingState()
    object InProgress : ParsingState()
    object Success : ParsingState()
    data class Error(val message: String) : ParsingState()
}