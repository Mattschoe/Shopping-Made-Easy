package weberstudio.app.billigsteprodukter.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

class OfflineSettingsRepository(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
    }

    override val theme = dataStore.data.map { preferences ->
        val themeName = preferences[PreferencesKeys.THEME] ?: Theme.SYSTEM.name
        Theme.valueOf(themeName)
    }

    override suspend fun setTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }
}