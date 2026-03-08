package cc.cyliu.kegels.ui.stats

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cc.cyliu.kegels.R
import cc.cyliu.kegels.data.model.MILESTONE_THRESHOLDS

private data class MedalDisplay(@StringRes val nameRes: Int, val color: Color)

private val MEDAL_DISPLAY = mapOf(
    100     to MedalDisplay(R.string.medal_starter,    Color(0xFFCD7F32)),
    500     to MedalDisplay(R.string.medal_motivated,  Color(0xFFB87333)),
    1_000   to MedalDisplay(R.string.medal_consistent, Color(0xFFA8A9AD)),
    2_000   to MedalDisplay(R.string.medal_dedicated,  Color(0xFF607D8B)),
    5_000   to MedalDisplay(R.string.medal_expert,     Color(0xFFFFD700)),
    10_000  to MedalDisplay(R.string.medal_master,     Color(0xFFFF8F00)),
    20_000  to MedalDisplay(R.string.medal_champion,   Color(0xFF26A69A)),
    50_000  to MedalDisplay(R.string.medal_legend,     Color(0xFF1E88E5)),
    100_000 to MedalDisplay(R.string.medal_immortal,   Color(0xFF8E24AA)),
)

fun formatThreshold(count: Int): String = if (count >= 1_000) "${count / 1_000}K" else "$count"

fun medalNameRes(threshold: Int): Int = MEDAL_DISPLAY[threshold]?.nameRes ?: R.string.medal_starter

@Composable
fun MedalGrid(
    unlockedThresholds: Set<Int>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        MILESTONE_THRESHOLDS.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { threshold ->
                    MedalBadge(threshold = threshold, unlocked = threshold in unlockedThresholds)
                }
                // Pad incomplete last row so spacing stays even
                repeat(3 - row.size) {
                    Spacer(Modifier.size(64.dp))
                }
            }
        }
    }
}

@Composable
fun MedalBadge(
    threshold: Int,
    unlocked: Boolean,
    size: Dp = 64.dp
) {
    val display = MEDAL_DISPLAY[threshold] ?: return
    val ringColor  = if (unlocked) display.color else Color(0xFF9E9E9E).copy(alpha = 0.45f)
    val bgColor    = if (unlocked) display.color.copy(alpha = 0.15f) else Color(0xFF9E9E9E).copy(alpha = 0.08f)
    val labelColor = if (unlocked) display.color else Color(0xFF9E9E9E).copy(alpha = 0.4f)
    val nameColor  = if (unlocked) MaterialTheme.colorScheme.onSurface
                     else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor)
                .border(2.5.dp, ringColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatThreshold(threshold),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = labelColor
            )
        }
        Text(
            text = stringResource(display.nameRes),
            style = MaterialTheme.typography.labelSmall,
            color = nameColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
