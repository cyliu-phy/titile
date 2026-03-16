package cc.cyliu.kegels.ui.settings

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.cyliu.kegels.data.datastore.AppPreferences
import cc.cyliu.kegels.data.model.ExerciseConfig
import cc.cyliu.kegels.data.repository.BackupRepository
import cc.cyliu.kegels.data.repository.ExerciseConfigRepository
import cc.cyliu.kegels.data.repository.NotificationSettings
import cc.cyliu.kegels.data.repository.NotificationSettingsRepository
import cc.cyliu.kegels.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object Saved : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

sealed class BackupUiState {
    object Idle : BackupUiState()
    data class Success(val count: Int, val isImport: Boolean) : BackupUiState()
    object Failure : BackupUiState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val exerciseConfigRepository: ExerciseConfigRepository,
    private val notificationSettingsRepository: NotificationSettingsRepository,
    private val notificationScheduler: NotificationScheduler,
    private val backupRepository: BackupRepository
) : ViewModel() {

    val currentLanguage: StateFlow<String> = dataStore.data
        .map { prefs -> prefs[AppPreferences.LANGUAGE_TAG] ?: "system" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    fun saveLanguage(tag: String) {
        viewModelScope.launch {
            dataStore.edit { it[AppPreferences.LANGUAGE_TAG] = tag }
        }
    }

    val config: StateFlow<ExerciseConfig> = exerciseConfigRepository.config
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExerciseConfig())

    val notificationSettings: StateFlow<NotificationSettings> =
        notificationSettingsRepository.settings
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationSettings())

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _backupUiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val backupUiState: StateFlow<BackupUiState> = _backupUiState.asStateFlow()

    fun saveConfig(kegelsPerMinute: Int, totalKegels: Int) {
        if (kegelsPerMinute !in 10..120 || totalKegels !in 10..300) {
            _uiState.value = SettingsUiState.Error("settings_error_range")
            return
        }
        viewModelScope.launch {
            exerciseConfigRepository.saveConfig(kegelsPerMinute, totalKegels)
            _uiState.value = SettingsUiState.Saved
        }
    }

    fun saveIntervalMinutes(minutes: Int) {
        viewModelScope.launch {
            notificationSettingsRepository.saveIntervalMinutes(minutes)
            notificationScheduler.reschedule()
        }
    }

    fun saveNightMode(
        enabled: Boolean,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int
    ) {
        viewModelScope.launch {
            notificationSettingsRepository.saveNightMode(
                enabled, startHour, startMinute, endHour, endMinute
            )
            notificationScheduler.reschedule()
        }
    }

    fun clearUiState() {
        _uiState.value = SettingsUiState.Idle
    }

    fun clearBackupUiState() {
        _backupUiState.value = BackupUiState.Idle
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { backupRepository.exportCsv(uri) } }
                .onSuccess { count -> _backupUiState.value = BackupUiState.Success(count, isImport = false) }
                .onFailure { _backupUiState.value = BackupUiState.Failure }
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { backupRepository.exportBackup(uri) } }
                .onSuccess { count -> _backupUiState.value = BackupUiState.Success(count, isImport = false) }
                .onFailure { _backupUiState.value = BackupUiState.Failure }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { backupRepository.importBackup(uri) } }
                .onSuccess { count -> _backupUiState.value = BackupUiState.Success(count, isImport = true) }
                .onFailure { _backupUiState.value = BackupUiState.Failure }
        }
    }
}
