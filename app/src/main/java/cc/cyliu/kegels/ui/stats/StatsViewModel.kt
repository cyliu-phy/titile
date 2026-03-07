package cc.cyliu.kegels.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.cyliu.kegels.data.db.DailyTotal
import cc.cyliu.kegels.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class StatsUiState(
    val dailyTotals: Map<String, Int> = emptyMap(),
    val hourlyDistribution: List<Int> = List(24) { 0 },
    val weeklyTotals: List<DailyTotal> = emptyList(),
    val todayTotal: Int = 0,
    val weekTotal: Int = 0,
    val allTimeTotal: Int = 0,
    val streakDays: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    sessionRepository: SessionRepository
) : ViewModel() {

    private val today = LocalDate.now()
    private val todayStr = today.toString()
    private val weekAgoStr = today.minusDays(6).toString()
    private val threeMonthsAgoStr = today.minusDays(90).toString()

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

        StatsUiState(
            dailyTotals = dailyMap,
            hourlyDistribution = hourly,
            weeklyTotals = weeklyList,
            todayTotal = todayTotal,
            weekTotal = weekTotal,
            allTimeTotal = allTime,
            streakDays = calculateStreak(dailyMap)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

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
