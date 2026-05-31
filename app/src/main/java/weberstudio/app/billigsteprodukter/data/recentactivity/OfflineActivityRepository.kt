package weberstudio.app.billigsteprodukter.data.recentactivity

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.RecentActivity

class OfflineActivityRepository(private val dao: RecentActivityDao) : ActivityRepository {
    override fun getRecentActivities(): Flow<List<RecentActivity>> {
        return dao.getRecentActivities()
    }

    override suspend fun insertActivity(activity: RecentActivity) {
        dao.insertActivity(activity)
        //Hold tabellen lille — UI viser kun de nyeste få aktiviteter
        dao.trimToLatest(MAX_ACTIVITIES)
    }

    private companion object {
        const val MAX_ACTIVITIES = 20
    }
}