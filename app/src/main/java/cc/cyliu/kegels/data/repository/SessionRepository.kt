package cc.cyliu.kegels.data.repository

import cc.cyliu.kegels.data.db.DailyTotal
import cc.cyliu.kegels.data.db.HourlyCount
import cc.cyliu.kegels.data.db.SessionDao
import cc.cyliu.kegels.data.db.SessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    suspend fun logSession(
        startTimeMs: Long,
        endTimeMs: Long,
        plannedCount: Int,
        completedCount: Int
    ) {
        if (completedCount == 0) return
        val zone = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val startDt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeMs), zone)
        val endDt = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimeMs), zone)
        sessionDao.insert(
            SessionEntity(
                date = startDt.toLocalDate().toString(),
                startTime = startDt.format(formatter),
                endTime = endDt.format(formatter),
                plannedCount = plannedCount,
                completedCount = completedCount,
                durationSeconds = (endTimeMs - startTimeMs) / 1000
            )
        )
    }

    fun getDailyTotals(from: String, to: String): Flow<List<DailyTotal>> =
        sessionDao.getDailyTotals(from, to)

    fun getHourlyDistribution(): Flow<List<HourlyCount>> =
        sessionDao.getHourlyDistribution()

    fun getWeeklyTotals(weekAgo: String): Flow<List<DailyTotal>> =
        sessionDao.getWeeklyTotals(weekAgo)

    fun getAllTimeTotal(): Flow<Int?> =
        sessionDao.getAllTimeTotal()

    suspend fun getAllSessions(): List<SessionEntity> = sessionDao.getAllSessions()

    suspend fun insertAll(sessions: List<SessionEntity>) = sessionDao.insertAll(sessions)
}
