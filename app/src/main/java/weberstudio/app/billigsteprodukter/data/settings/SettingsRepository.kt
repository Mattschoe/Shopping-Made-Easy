package weberstudio.app.billigsteprodukter.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val theme: Flow<Theme>
    val coop365Option: Flow<Coop365Option.Option?>
    val totalOption: Flow<TotalOption>
    val hasCompletedOnboarding: Flow<Boolean>
    val hasBeenWarnedAboutReceiptReadability: Flow<Boolean>
    val hasVisitedReceiptPage: Flow<Boolean>
    suspend fun setTheme(theme: Theme)
    suspend fun setCoop365Option(option: Coop365Option.Option)
    suspend fun setTotalOption(option: TotalOption)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setHasBeenWarnedAboutScanReadability(hasBeenWarned: Boolean)
    suspend fun setHasVisitedReceiptPage(hasVisited: Boolean)
    suspend fun deleteAllProducts()
}

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM;

    override fun toString(): String {
        return when (this) {
            DARK -> "Mørk tilstand"
            LIGHT -> "Lys tilstand"
            SYSTEM -> "System standard"
        }
    }
}

data class Coop365Option(
    val type: Option,
    val image: Int
) {
    enum class Option {
        OVER,
        UNDER;

        override fun toString(): String {
            return when(this) {
                OVER -> "Mængde over"
                UNDER -> "Mængde under"
            }
        }
    }
}

enum class TotalOption {
    RECEIPT_TOTAL,
    PRODUCT_TOTAL;

    override fun toString(): String {
        return when (this) {
            RECEIPT_TOTAL -> "Kvitteringstotal"
            PRODUCT_TOTAL -> "Produkttotal"
        }
    }
}