package cc.cyliu.kegels.notifications

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cc.cyliu.kegels.data.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val notificationSettingsRepository: NotificationSettingsRepository
) {
    suspend fun reschedule() {
        workManager.cancelAllWorkByTag(TAG)

        val settings = notificationSettingsRepository.settings.first()
        if (settings.intervalMinutes <= 0) return

        val now = LocalDateTime.now()
        val nightStart = LocalTime.of(settings.nightStartHour, settings.nightStartMinute)
        val nightEnd = LocalTime.of(settings.nightEndHour, settings.nightEndMinute)

        var next = now.plusMinutes(settings.intervalMinutes.toLong())
        var slotsScheduled = 0

        while (slotsScheduled < 48) {
            val inNight = settings.nightModeEnabled &&
                    isInNightWindow(next.toLocalTime(), nightStart, nightEnd)

            if (!inNight) {
                val delayMs = Duration.between(now, next).toMillis().coerceAtLeast(0L)
                val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .addTag(TAG)
                    .build()
                workManager.enqueue(request)
                slotsScheduled++
            }
            next = next.plusMinutes(settings.intervalMinutes.toLong())
        }
    }

    private fun isInNightWindow(time: LocalTime, start: LocalTime, end: LocalTime): Boolean {
        return if (start <= end) {
            time >= start && time < end
        } else {
            // Wraps midnight, e.g. 22:00–07:00
            time >= start || time < end
        }
    }

    companion object {
        const val TAG = "kegel_reminder"
    }
}
