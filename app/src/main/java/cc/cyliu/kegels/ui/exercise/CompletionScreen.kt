package cc.cyliu.kegels.ui.exercise

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cc.cyliu.kegels.R
import cc.cyliu.kegels.Screen
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CompletionScreen(
    navController: NavController,
    totalCount: Int,
    durationSeconds: Long
) {
    var iconScale by remember { mutableFloatStateOf(0f) }
    val animatedScale by animateFloatAsState(
        targetValue = iconScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "iconScale"
    )
    LaunchedEffect(Unit) { iconScale = 1f }

    val durationMin = (durationSeconds / 60).toInt()
    val durationSec = (durationSeconds % 60).toInt()
    val timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✅",
            fontSize = 72.sp,
            modifier = Modifier.scale(animatedScale)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.completion_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                SummaryRow(
                    label = stringResource(R.string.completion_kegels),
                    value = "$totalCount"
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                SummaryRow(
                    label = stringResource(R.string.completion_duration),
                    value = stringResource(R.string.session_duration_fmt, durationMin, durationSec)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                SummaryRow(label = "Time", value = timestamp)
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.completion_done))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
