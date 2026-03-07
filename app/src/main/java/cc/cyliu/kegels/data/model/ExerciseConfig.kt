package cc.cyliu.kegels.data.model

data class ExerciseConfig(
    val kegelsPerMinute: Int = 60,
    val totalKegels: Int = 100
) {
    val intervalMs: Long get() = 60_000L / kegelsPerMinute
    val durationSeconds: Long get() = (totalKegels * intervalMs) / 1000
}
