package weberstudio.app.billigsteprodukter.data.recentactivity

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.RecentActivity

class OfflineActivityRepository(private val dao: RecentActivityDao) : ActivityRepository {
    override fun getRecentActivities(): Flow<List<RecentActivity>> {
        return dao.getRecentActivities()
    }

    override fun getAllActivities(): Flow<List<RecentActivity>> {
        return dao.getAllActivities()
    }

    override suspend fun insertActivity(activity: RecentActivity) {
        return dao.insertActivity(activity)
    }

    override suspend fun deleteActivity(id: String) {
        return dao.deleteActivity(id)
    }
}