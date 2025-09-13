package weberstudio.app.billigsteprodukter.data.recentactivity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import weberstudio.app.billigsteprodukter.data.RecentActivity

@Dao
interface RecentActivityDao {
    /**
     * Returns the last 5 activites
     */
    @Query("SELECT * FROM recent_activities ORDER BY timestamp DESC LIMIT 5")
    fun getRecentActivites(): Flow<List<RecentActivity>>

    @Insert
    suspend fun insertActivity(activity: RecentActivity)

    @Query("DELETE FROM recent_activities WHERE id = :id")
    suspend fun deleteActivity(id: String)
}