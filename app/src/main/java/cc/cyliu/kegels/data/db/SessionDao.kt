package cc.cyliu.kegels.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class DailyTotal(val date: String, val total: Int)
data class HourlyCount(val hour: String, val count: Int)

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: SessionEntity)

    @Query("""
        SELECT date, SUM(completedCount) as total
        FROM sessions
        WHERE date BETWEEN :from AND :to
        GROUP BY date
    """)
    fun getDailyTotals(from: String, to: String): Flow<List<DailyTotal>>

    @Query("""
        SELECT strftime('%H', startTime) as hour, SUM(completedCount) as count
        FROM sessions
        GROUP BY hour
    """)
    fun getHourlyDistribution(): Flow<List<HourlyCount>>

    @Query("""
        SELECT date, SUM(completedCount) as total
        FROM sessions
        WHERE date >= :weekAgo
        GROUP BY date
        ORDER BY date
    """)
    fun getWeeklyTotals(weekAgo: String): Flow<List<DailyTotal>>

    @Query("SELECT SUM(completedCount) FROM sessions WHERE date = :date")
    fun getTodayTotal(date: String): Flow<Int?>

    @Query("SELECT SUM(completedCount) FROM sessions")
    fun getAllTimeTotal(): Flow<Int?>

    @Query("SELECT * FROM sessions ORDER BY startTime ASC")
    suspend fun getAllSessions(): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(sessions: List<SessionEntity>)
}
