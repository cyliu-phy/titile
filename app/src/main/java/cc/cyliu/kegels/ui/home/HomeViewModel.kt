package cc.cyliu.kegels.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.cyliu.kegels.data.repository.NotificationSettingsRepository
import cc.cyliu.kegels.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    notificationSettingsRepository: NotificationSettingsRepository
) : ViewModel() {

    private val todayStr = LocalDate.now().toString()

    val todayTotal: StateFlow<Int> = sessionRepository
        .getDailyTotals(todayStr, todayStr)
        .map { list -> list.firstOrNull()?.total ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val nextReminderTime: StateFlow<String?> = notificationSettingsRepository.settings
        .map { settings ->
            if (settings.intervalMinutes <= 0) return@map null
            val nightStart = LocalTime.of(settings.nightStartHour, settings.nightStartMinute)
            val nightEnd = LocalTime.of(settings.nightEndHour, settings.nightEndMinute)
            var candidate = LocalTime.now().plusMinutes(settings.intervalMinutes.toLong())
            if (settings.nightModeEnabled) {
                var attempts = 0
                while (isInNightWindow(candidate, nightStart, nightEnd) && attempts < 48) {
                    candidate = candidate.plusMinutes(settings.intervalMinutes.toLong())
                    attempts++
                }
                if (attempts >= 48) return@map null
            }
            candidate.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun isInNightWindow(time: LocalTime, start: LocalTime, end: LocalTime): Boolean =
        if (start <= end) time >= start && time < end
        else time >= start || time < end
}
