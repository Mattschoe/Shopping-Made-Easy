package weberstudio.app.billigsteprodukter.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val theme: Flow<Theme>
    val coop365Option: Flow<Coop365Option.Option?>
    suspend fun setTheme(theme: Theme)
    suspend fun setCoop365Option(option: Coop365Option.Option)
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