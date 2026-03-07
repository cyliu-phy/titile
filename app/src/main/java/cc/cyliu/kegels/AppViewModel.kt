package cc.cyliu.kegels

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.cyliu.kegels.data.datastore.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    /** null = still loading, false = needs onboarding, true = ready */
    val isOnboardingComplete: StateFlow<Boolean?> = dataStore.data
        .map { prefs -> prefs[AppPreferences.ONBOARDING_COMPLETE] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStore.edit { it[AppPreferences.ONBOARDING_COMPLETE] = true }
        }
    }

    fun saveLanguage(tag: String) {
        viewModelScope.launch {
            dataStore.edit { it[AppPreferences.LANGUAGE_TAG] = tag }
        }
    }
}
