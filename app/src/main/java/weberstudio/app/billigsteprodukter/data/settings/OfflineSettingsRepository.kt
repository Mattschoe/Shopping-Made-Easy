package weberstudio.app.billigsteprodukter.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
        val COOP365_OPTION = stringPreferencesKey("coop365_option")
        val TOTAL_OPTION = stringPreferencesKey("total_option")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val HAS_BEEN_WARNED_ABOUT_RECEIPT_READABILITY = booleanPreferencesKey("receipt_scan_readability")
        val HAS_VISITED_RECEIPT_PAGE = booleanPreferencesKey("visited_receipt_page")
        val CAMERA_LAUNCH_REQUEST = booleanPreferencesKey("camera_launch_request")
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
        val optionName = preferences[PreferencesKeys.COOP365_OPTION]
        if (optionName == null) null
        else Coop365Option.Option.valueOf(optionName)
    }

    override val totalOption = dataStore.data.map { preferences ->
        val optionName = preferences[PreferencesKeys.TOTAL_OPTION] ?: TotalOption.PRODUCT_TOTAL.name
        TotalOption.valueOf(optionName)
    }

    override val hasCompletedOnboarding = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] == true
    }

    override val hasBeenWarnedAboutReceiptReadability = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_BEEN_WARNED_ABOUT_RECEIPT_READABILITY] == true
    }

    override val hasVisitedReceiptPage = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.HAS_VISITED_RECEIPT_PAGE] == true
    }

    override val cameraLaunchRequest = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CAMERA_LAUNCH_REQUEST] == true
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
            preferences[PreferencesKeys.COOP365_OPTION] = option.name
        }
    }


    override suspend fun setTotalOption(option: TotalOption) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOTAL_OPTION] = option.name
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    override suspend fun setHasBeenWarnedAboutScanReadability(hasBeenWarned: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_BEEN_WARNED_ABOUT_RECEIPT_READABILITY] = hasBeenWarned
        }
    }

    override suspend fun setHasVisitedReceiptPage(hasVisited: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_VISITED_RECEIPT_PAGE] = hasVisited
        }
    }

    override suspend fun setCameraLaunchRequest(requestLaunch: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAMERA_LAUNCH_REQUEST] = requestLaunch
        }
    }
    //endregion
}