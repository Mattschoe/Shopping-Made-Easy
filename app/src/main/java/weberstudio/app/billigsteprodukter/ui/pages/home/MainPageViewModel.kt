package weberstudio.app.billigsteprodukter.ui.pages.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ReceiptApp

class MainPageViewModel(application: Application): AndroidViewModel(application) {
    private val app = application as ReceiptApp
    private val settingsRepo = app.settingsRepository

    val hasCompletedOnboarding = settingsRepo.hasCompletedOnboarding
    val hasVisitedReceiptPage = settingsRepo.hasVisitedReceiptPage
    val hasBeenWarnedAboutReceiptReadability = settingsRepo.hasBeenWarnedAboutReceiptReadability

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            settingsRepo.setOnboardingCompleted(completed)
        }
    }

    fun setHasVisitedReceiptPage(hasVisited: Boolean) {
        viewModelScope.launch {
            settingsRepo.setHasVisitedReceiptPage(hasVisited)
        }
    }

    fun setHasBeenWarnedAboutReceipt(beenWarned: Boolean) {
        viewModelScope.launch {
            settingsRepo.setHasBeenWarnedAboutScanReadability(beenWarned)
        }
    }

}