package cc.cyliu.kegels.data.repository

import android.content.Context
import android.net.Uri
import cc.cyliu.kegels.data.db.SessionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository
) {
    /**
     * Writes all sessions to a CSV file at [uri].
     * Returns the number of rows written.
     */
    suspend fun exportCsv(uri: Uri): Int {
        val sessions = sessionRepository.getAllSessions()
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { w ->
            w.write("id,date,startTime,endTime,plannedCount,completedCount,durationSeconds\n")
            sessions.forEach { s ->
                w.write("${s.id},${s.date},${s.startTime},${s.endTime},${s.plannedCount},${s.completedCount},${s.durationSeconds}\n")
            }
        }
        return sessions.size
    }

    /**
     * Serialises all sessions as a JSON array and writes to [uri].
     * Returns the number of sessions backed up.
     */
    suspend fun exportBackup(uri: Uri): Int {
        val sessions = sessionRepository.getAllSessions()
        val array = JSONArray()
        sessions.forEach { s ->
            array.put(JSONObject().apply {
                put("id", s.id)
                put("date", s.date)
                put("startTime", s.startTime)
                put("endTime", s.endTime)
                put("plannedCount", s.plannedCount)
                put("completedCount", s.completedCount)
                put("durationSeconds", s.durationSeconds)
            })
        }
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { w ->
            w.write(array.toString())
        }
        return sessions.size
    }

    /**
     * Reads a JSON backup from [uri] and inserts the sessions into the database.
     * Existing rows with the same primary key are ignored (no duplicates).
     * Returns the number of sessions parsed.
     */
    suspend fun importBackup(uri: Uri): Int {
        val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
            it.readText()
        } ?: return 0

        val array = JSONArray(json)
        val sessions = mutableListOf<SessionEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            sessions.add(
                SessionEntity(
                    id = obj.getLong("id"),
                    date = obj.getString("date"),
                    startTime = obj.getString("startTime"),
                    endTime = obj.getString("endTime"),
                    plannedCount = obj.getInt("plannedCount"),
                    completedCount = obj.getInt("completedCount"),
                    durationSeconds = obj.getLong("durationSeconds")
                )
            )
        }
        sessionRepository.insertAll(sessions)
        return sessions.size
    }
}
