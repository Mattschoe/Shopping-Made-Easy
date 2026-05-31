package weberstudio.app.billigsteprodukter.data.recentactivity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.RecentActivity

@Dao
interface RecentActivityDao {
    /**
     * Returns the last 5 activities ordered by their timestamp
     */
    @Query("SELECT * FROM recent_activities ORDER BY timestamp DESC LIMIT 5")
    fun getRecentActivities(): Flow<List<RecentActivity>>

    @Insert
    suspend fun insertActivity(activity: RecentActivity)

    /**
     * Sletter alle aktiviteter på nær de [cap] nyeste, så tabellen ikke vokser ubegrænset.
     */
    @Query(
        "DELETE FROM recent_activities WHERE id NOT IN " +
        "(SELECT id FROM recent_activities ORDER BY timestamp DESC, id DESC LIMIT :cap)"
    )
    suspend fun trimToLatest(cap: Int)
}