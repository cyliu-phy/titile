package cc.cyliu.kegels.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import java.time.LocalDate

private val HeatmapGreen = Color(0xFF388E3C)
private val CellSize = 14.dp
private val CellSpacing = 3.dp

@Composable
fun HeatmapGrid(
    dailyTotals: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val startDay = remember { today.minusDays(90) }
    // leadingPad: days before startDay to reach the nearest preceding Sunday (Sun=0)
    val leadingPad = remember { startDay.dayOfWeek.value % 7 }
    val days = remember { (0..90).map { startDay.plusDays(it.toLong()) } }
    val numWeeks = remember { (leadingPad + days.size + 6) / 7 }

    val maxCount = remember(dailyTotals) {
        dailyTotals.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    }
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant

    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }

    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(modifier = modifier) {
        // Heatmap: day labels on the left, week columns scrolling right
        Row(verticalAlignment = Alignment.Top) {
            // Vertical day-of-week axis
            Column(verticalArrangement = Arrangement.spacedBy(CellSpacing)) {
                dayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.size(CellSize),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            // Week columns (horizontally scrollable for safety on narrow screens)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(CellSpacing)
            ) {
                for (week in 0 until numWeeks) {
                    Column(verticalArrangement = Arrangement.spacedBy(CellSpacing)) {
                        for (dayOfWeek in 0..6) {
                            val cellIndex = week * 7 + dayOfWeek
                            val day = if (cellIndex < leadingPad) null
                                      else days.getOrNull(cellIndex - leadingPad)
                            Box(modifier = Modifier.size(CellSize)) {
                                if (day != null) {
                                    val count = dailyTotals[day.toString()] ?: 0
                                    val intensity = count.toFloat() / maxCount
                                    val color = lerp(emptyColor, HeatmapGreen, intensity)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color, RoundedCornerShape(3.dp))
                                            .clickable {
                                                selectedDay = if (selectedDay == day) null else day
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Always reserve space for the tooltip so layout doesn't shift on selection
        val tooltipText = selectedDay?.let { day ->
            val count = dailyTotals[day.toString()] ?: 0
            "$day — $count kegels"
        } ?: ""
        Spacer(Modifier.height(8.dp))
        Text(
            text = tooltipText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
