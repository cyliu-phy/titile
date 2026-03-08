package cc.cyliu.kegels.ui.stats

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.cyliu.kegels.data.datastore.AppPreferences
import cc.cyliu.kegels.data.db.DailyTotal
import cc.cyliu.kegels.data.model.MILESTONE_THRESHOLDS
import cc.cyliu.kegels.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class StatsUiState(
    val dailyTotals: Map<String, Int> = emptyMap(),
    val hourlyDistribution: List<Int> = List(24) { 0 },
    val weeklyTotals: List<DailyTotal> = emptyList(),
    val todayTotal: Int = 0,
    val weekTotal: Int = 0,
    val allTimeTotal: Int = 0,
    val streakDays: Int = 0,
    val unlockedThresholds: Set<Int> = emptySet(),
    val pendingThreshold: Int? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    sessionRepository: SessionRepository
) : ViewModel() {

    private val today = LocalDate.now()
    private val todayStr = today.toString()
    private val weekAgoStr = today.minusDays(6).toString()
    private val threeMonthsAgoStr = today.minusDays(90).toString()

    private val acknowledgedMilestones: Flow<Set<Int>> = dataStore.data.map { prefs ->
        prefs[AppPreferences.ACKNOWLEDGED_MILESTONES]
            ?.split(",")?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    val uiState: StateFlow<StatsUiState> = combine(
        sessionRepository.getDailyTotals(threeMonthsAgoStr, todayStr),
        sessionRepository.getHourlyDistribution(),
        sessionRepository.getWeeklyTotals(weekAgoStr),
        sessionRepository.getAllTimeTotal().map { it ?: 0 }
    ) { dailyList, hourlyList, weeklyList, allTime ->
        val dailyMap = dailyList.associate { it.date to it.total }
        val hourly = List(24) { hour ->
            hourlyList.firstOrNull { it.hour == "%02d".format(hour) }?.count ?: 0
        }
        val weekTotal = weeklyList.sumOf { it.total }
        val todayTotal = dailyMap[todayStr] ?: 0
        val unlockedThresholds = MILESTONE_THRESHOLDS.filter { it <= allTime }.toSet()
        StatsUiState(
            dailyTotals = dailyMap,
            hourlyDistribution = hourly,
            weeklyTotals = weeklyList,
            todayTotal = todayTotal,
            weekTotal = weekTotal,
            allTimeTotal = allTime,
            streakDays = calculateStreak(dailyMap),
            unlockedThresholds = unlockedThresholds
        )
    }.combine(acknowledgedMilestones) { state, acked ->
        val pending = state.unlockedThresholds.filter { it !in acked }.maxOrNull()
        state.copy(pendingThreshold = pending)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun acknowledgeMilestone(threshold: Int) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                val current = prefs[AppPreferences.ACKNOWLEDGED_MILESTONES]
                    ?.split(",")?.filter { it.isNotEmpty() }?.toMutableSet() ?: mutableSetOf()
                current.add(threshold.toString())
                prefs[AppPreferences.ACKNOWLEDGED_MILESTONES] = current.joinToString(",")
            }
        }
    }

    private fun calculateStreak(dailyTotals: Map<String, Int>): Int {
        var streak = 0
        var date = LocalDate.now()
        while (true) {
            if ((dailyTotals[date.toString()] ?: 0) > 0) {
                streak++
                date = date.minusDays(1)
            } else break
        }
        return streak
    }
}
