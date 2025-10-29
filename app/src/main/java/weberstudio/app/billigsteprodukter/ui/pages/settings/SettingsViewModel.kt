package weberstudio.app.billigsteprodukter.ui.pages.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.settings.Coop365Option
import weberstudio.app.billigsteprodukter.data.settings.SettingsRepository
import weberstudio.app.billigsteprodukter.data.settings.Theme

class SettingsViewModel(application: Application): AndroidViewModel(application) {
    private val app = application as ReceiptApp
    private val settingsRepo: SettingsRepository = app.settingsRepository

    val theme = settingsRepo.theme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Theme.SYSTEM
    )

    fun setTheme(theme: Theme) {
        viewModelScope.launch {
            settingsRepo.setTheme(theme)
        }
    }

    fun setCoop365Option(option: Coop365Option.Option) {
        viewModelScope.launch {
            settingsRepo.setCoop365Option(option)
        }
    }
}