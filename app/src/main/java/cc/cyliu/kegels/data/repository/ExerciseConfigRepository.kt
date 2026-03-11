package cc.cyliu.kegels.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import cc.cyliu.kegels.data.datastore.ExercisePreferences
import cc.cyliu.kegels.data.model.ExerciseConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseConfigRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val config: Flow<ExerciseConfig> = dataStore.data.map { prefs ->
        ExerciseConfig(
            kegelsPerMinute = prefs[ExercisePreferences.KEGELS_PER_MINUTE] ?: 25,
            totalKegels = prefs[ExercisePreferences.TOTAL_KEGELS] ?: 100
        )
    }

    suspend fun saveConfig(kegelsPerMinute: Int, totalKegels: Int) {
        dataStore.edit { prefs ->
            prefs[ExercisePreferences.KEGELS_PER_MINUTE] = kegelsPerMinute
            prefs[ExercisePreferences.TOTAL_KEGELS] = totalKegels
        }
    }
}
