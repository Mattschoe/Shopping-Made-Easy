package weberstudio.app.billigsteprodukter.logic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.recentactivity.OfflineActivityRepository

class ActivityViewModel(application: Application): AndroidViewModel(application) {
    private val activityRepo: OfflineActivityRepository = (application as ReceiptApp).activityRepository

    val recentActivities = activityRepo.getRecentActivities()
}