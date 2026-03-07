package cc.cyliu.kegels.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.cyliu.kegels.data.model.ExerciseConfig
import cc.cyliu.kegels.data.repository.ExerciseConfigRepository
import cc.cyliu.kegels.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Phase { CONTRACT, RELAX }

sealed class SessionState {
    object Idle : SessionState()
    data class Running(val currentCount: Int, val phase: Phase) : SessionState()
    data class Paused(val currentCount: Int) : SessionState()
    data class Completed(val totalCount: Int, val durationSeconds: Long) : SessionState()
}

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    exerciseConfigRepository: ExerciseConfigRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    val config: StateFlow<ExerciseConfig> = exerciseConfigRepository.config
        .stateIn(viewModelScope, SharingStarted.Eagerly, ExerciseConfig())

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Idle)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private var sessionJob: Job? = null
    private var tickerJob: Job? = null
    private var pausedCount: Int = 0
    private var sessionStartTime: Long = 0L
    private var sessionPlannedCount: Int = 0

    fun start() {
        if (_sessionState.value !is SessionState.Idle) return
        sessionStartTime = System.currentTimeMillis()
        sessionPlannedCount = config.value.totalKegels
        _elapsedSeconds.value = 0L
        startTicker()
        launchSession(startCount = 0)
    }

    fun pause() {
        val current = _sessionState.value as? SessionState.Running ?: return
        pausedCount = current.currentCount
        sessionJob?.cancel()
        tickerJob?.cancel()
        _sessionState.value = SessionState.Paused(pausedCount)
    }

    fun resume() {
        if (_sessionState.value !is SessionState.Paused) return
        startTicker()
        launchSession(startCount = pausedCount)
    }

    fun stop() {
        val count = when (val s = _sessionState.value) {
            is SessionState.Running -> s.currentCount
            is SessionState.Paused -> s.currentCount
            else -> return  // already completed or idle — no-op
        }
        sessionJob?.cancel()
        tickerJob?.cancel()
        val endTime = System.currentTimeMillis()
        viewModelScope.launch {
            sessionRepository.logSession(
                startTimeMs = sessionStartTime,
                endTimeMs = endTime,
                plannedCount = sessionPlannedCount,
                completedCount = count
            )
        }
        val duration = (endTime - sessionStartTime) / 1000
        _sessionState.value = SessionState.Completed(count, duration)
    }

    fun reset() {
        sessionJob?.cancel()
        tickerJob?.cancel()
        _sessionState.value = SessionState.Idle
        _elapsedSeconds.value = 0L
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _elapsedSeconds.value++
            }
        }
    }

    private fun launchSession(startCount: Int) {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            val cfg = config.value
            val totalKegels = cfg.totalKegels
            val intervalMs = cfg.intervalMs
            var currentCount = startCount

            while (currentCount < totalKegels) {
                _sessionState.value = SessionState.Running(currentCount, Phase.CONTRACT)
                delay(intervalMs / 2)
                _sessionState.value = SessionState.Running(currentCount, Phase.RELAX)
                delay(intervalMs / 2)
                currentCount++
            }

            tickerJob?.cancel()
            val endTime = System.currentTimeMillis()
            sessionRepository.logSession(
                startTimeMs = sessionStartTime,
                endTimeMs = endTime,
                plannedCount = sessionPlannedCount,
                completedCount = totalKegels
            )
            val duration = (endTime - sessionStartTime) / 1000
            _sessionState.value = SessionState.Completed(totalKegels, duration)
        }
    }
}
