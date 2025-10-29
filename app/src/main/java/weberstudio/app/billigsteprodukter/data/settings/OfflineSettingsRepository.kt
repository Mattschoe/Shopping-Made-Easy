package weberstudio.app.billigsteprodukter.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptDao

class OfflineSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val receiptDao: ReceiptDao
) : SettingsRepository {
    private object PreferencesKeys {
        val THEME = stringPreferencesKey("theme")
        val COOP365OPTION = stringPreferencesKey("coop365_option")
    }

    override suspend fun deleteAllProducts() {
        receiptDao.deleteAllProducts()
    }

    //region GETTERS
    override val theme = dataStore.data.map { preferences ->
        val themeName = preferences[PreferencesKeys.THEME] ?: Theme.SYSTEM.name
        Theme.valueOf(themeName)
    }

    override val coop365Option = dataStore.data.map { preferences ->
        val optionName = preferences[PreferencesKeys.COOP365OPTION]
        if (optionName == null) null
        else Coop365Option.Option.valueOf(optionName)
    }
    //endregion

    //region SETTERS
    override suspend fun setTheme(theme: Theme) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    override suspend fun setCoop365Option(option: Coop365Option.Option) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COOP365OPTION] = option.name
        }
    }
    //endregion
}