package weberstudio.app.billigsteprodukter.data.recentactivity

import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.RecentActivity

interface ActivityRepository {
    /**
     * Returns the last 5 activities ordered by their timestamp
     */
    fun getRecentActivities(): Flow<List<RecentActivity>>

    /**
     * Returns all activities ordered by their timestamp
     */
    fun getAllActivities(): Flow<List<RecentActivity>>

    suspend fun insertActivity(activity: RecentActivity)

    suspend fun deleteActivity(id: String)
}