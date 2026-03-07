package cc.cyliu.kegels.ui.exercise

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cc.cyliu.kegels.R

private val ColorContract = Color(0xFFE57373)
private val ColorRelax = Color(0xFF4DD0E1)

@Composable
fun CircleAnimationCanvas(
    sessionState: SessionState,
    intervalMs: Long,
    modifier: Modifier = Modifier
) {
    val phase = if (sessionState is SessionState.Running) sessionState.phase else Phase.RELAX
    val isIdle = sessionState is SessionState.Idle
    val halfPhaseDuration = (intervalMs / 2).toInt().coerceAtLeast(100)

    val targetRadius = when {
        isIdle -> 0.7f
        phase == Phase.CONTRACT -> 0.55f
        else -> 1.0f
    }
    val targetColor = if (phase == Phase.CONTRACT) ColorContract else ColorRelax

    val animatedRadius by animateFloatAsState(
        targetValue = targetRadius,
        animationSpec = tween(durationMillis = halfPhaseDuration, easing = FastOutSlowInEasing),
        label = "circleRadius"
    )
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = halfPhaseDuration),
        label = "circleColor"
    )

    val phaseLabelRes = when {
        isIdle || sessionState is SessionState.Paused -> null
        phase == Phase.CONTRACT -> R.string.exercise_contract
        else -> R.string.exercise_relax
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxRadius = size.minDimension / 2f
            val r = animatedRadius * maxRadius

            // Ripple rings
            drawCircle(color = animatedColor.copy(alpha = 0.14f), radius = r * 1.35f)
            drawCircle(color = animatedColor.copy(alpha = 0.07f), radius = r * 1.65f)

            // Main filled circle
            drawCircle(color = animatedColor, radius = r)

            // Subtle ring border
            drawCircle(
                color = animatedColor.copy(alpha = 0.6f),
                radius = r,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        if (phaseLabelRes != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(phaseLabelRes),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
