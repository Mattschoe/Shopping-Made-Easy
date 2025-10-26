package weberstudio.app.billigsteprodukter.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val theme: Flow<Theme>
    suspend fun setTheme(theme: Theme)
}

enum class Theme {
    LIGHT,
    DARK,
    SYSTEM;

    override fun toString(): String {
        return when (this) {
            Theme.DARK -> "Mørk tilstand"
            Theme.LIGHT -> "Lys tilstand"
            Theme.SYSTEM -> "System standard"
        }
    }
}