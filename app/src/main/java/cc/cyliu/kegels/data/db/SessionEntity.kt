package cc.cyliu.kegels.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,           // "YYYY-MM-DD"
    val startTime: String,      // ISO-8601 local date-time
    val endTime: String,
    val plannedCount: Int,
    val completedCount: Int,
    val durationSeconds: Long
)
