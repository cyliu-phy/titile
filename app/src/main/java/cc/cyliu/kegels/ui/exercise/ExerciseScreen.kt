package cc.cyliu.kegels.ui.exercise

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import cc.cyliu.kegels.R

@Composable
fun ExerciseScreen(
    navController: NavController,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsStateWithLifecycle()
    val config by viewModel.config.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showStopDialog by remember { mutableStateOf(false) }
    var wasRunningWhenStopPressed by remember { mutableStateOf(false) }

    // Navigate to CompletionScreen when session finishes
    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Completed) {
            val s = sessionState as SessionState.Completed
            context.getSystemService(Vibrator::class.java)
                ?.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
            navController.navigate("completion/${s.totalCount}/${s.durationSeconds}") {
                launchSingleTop = true
            }
            viewModel.reset()
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text(stringResource(R.string.exercise_stop_dialog_title)) },
            text = { Text(stringResource(R.string.exercise_stop_dialog_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showStopDialog = false
                    viewModel.stop()
                }) {
                    Text(stringResource(R.string.exercise_stop_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showStopDialog = false
                    if (wasRunningWhenStopPressed) viewModel.resume()
                }) {
                    Text(stringResource(R.string.exercise_stop_dialog_cancel))
                }
            }
        )
    }

    val currentCount = when (val s = sessionState) {
        is SessionState.Running -> s.currentCount
        is SessionState.Paused -> s.currentCount
        else -> 0
    }
    val isRunning = sessionState is SessionState.Running
    val isActive = sessionState is SessionState.Running || sessionState is SessionState.Paused

    when {
        sessionState is SessionState.Idle -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { viewModel.start() },
                    modifier = Modifier.size(200.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.exercise_start),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.exercise_idle_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        isActive -> {
            val progress = if (config.totalKegels > 0) {
                currentCount.toFloat() / config.totalKegels
            } else 0f
            val elapsedMin = elapsedSeconds / 60
            val elapsedSec = elapsedSeconds % 60

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(4.dp))

                CircleAnimationCanvas(
                    sessionState = sessionState,
                    intervalMs = config.intervalMs,
                    modifier = Modifier.size(280.dp)
                )

                Text(
                    text = stringResource(R.string.session_count_fmt, currentCount, config.totalKegels),
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "%02d:%02d".format(elapsedMin, elapsedSec),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = {
                        if (isRunning) viewModel.pause() else viewModel.resume()
                    }) {
                        Text(
                            stringResource(
                                if (isRunning) R.string.exercise_pause else R.string.exercise_resume
                            )
                        )
                    }
                    OutlinedButton(onClick = {
                        wasRunningWhenStopPressed = isRunning
                        if (isRunning) viewModel.pause()
                        showStopDialog = true
                    }) {
                        Text(stringResource(R.string.exercise_stop))
                    }
                }
            }
        }

        else -> {
            // Briefly visible while navigation to CompletionScreen is pending
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
