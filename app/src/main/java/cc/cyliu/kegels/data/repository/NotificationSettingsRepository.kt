package cc.cyliu.kegels.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import cc.cyliu.kegels.data.datastore.NotificationPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class NotificationSettings(
    val intervalMinutes: Int = 60,
    val nightModeEnabled: Boolean = true,
    val nightStartHour: Int = 22,
    val nightStartMinute: Int = 0,
    val nightEndHour: Int = 7,
    val nightEndMinute: Int = 0
)

@Singleton
class NotificationSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val settings: Flow<NotificationSettings> = dataStore.data.map { prefs ->
        NotificationSettings(
            intervalMinutes = prefs[NotificationPreferences.REMINDER_INTERVAL_MINUTES] ?: 60,
            nightModeEnabled = prefs[NotificationPreferences.NIGHT_MODE_ENABLED] ?: true,
            nightStartHour = prefs[NotificationPreferences.NIGHT_START_HOUR] ?: 22,
            nightStartMinute = prefs[NotificationPreferences.NIGHT_START_MINUTE] ?: 0,
            nightEndHour = prefs[NotificationPreferences.NIGHT_END_HOUR] ?: 7,
            nightEndMinute = prefs[NotificationPreferences.NIGHT_END_MINUTE] ?: 0
        )
    }

    suspend fun saveIntervalMinutes(minutes: Int) {
        dataStore.edit { it[NotificationPreferences.REMINDER_INTERVAL_MINUTES] = minutes }
    }

    suspend fun saveNightMode(
        enabled: Boolean,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) {
        dataStore.edit { prefs ->
            prefs[NotificationPreferences.NIGHT_MODE_ENABLED] = enabled
            prefs[NotificationPreferences.NIGHT_START_HOUR] = startHour
            prefs[NotificationPreferences.NIGHT_START_MINUTE] = startMinute
            prefs[NotificationPreferences.NIGHT_END_HOUR] = endHour
            prefs[NotificationPreferences.NIGHT_END_MINUTE] = endMinute
        }
    }
}
