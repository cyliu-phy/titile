package cc.cyliu.kegels.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kegel_prefs")

object ExercisePreferences {
    val KEGELS_PER_MINUTE = intPreferencesKey("kegels_per_minute")
    val TOTAL_KEGELS = intPreferencesKey("total_kegels")
}

object NotificationPreferences {
    val REMINDER_INTERVAL_MINUTES = intPreferencesKey("reminder_interval_minutes")
    val NIGHT_MODE_ENABLED = booleanPreferencesKey("night_mode_enabled")
    val NIGHT_START_HOUR = intPreferencesKey("night_start_hour")
    val NIGHT_START_MINUTE = intPreferencesKey("night_start_minute")
    val NIGHT_END_HOUR = intPreferencesKey("night_end_hour")
    val NIGHT_END_MINUTE = intPreferencesKey("night_end_minute")
}

object AppPreferences {
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val LANGUAGE_TAG = stringPreferencesKey("language_tag")
    val ACKNOWLEDGED_MILESTONES = stringPreferencesKey("acknowledged_milestones")
}
