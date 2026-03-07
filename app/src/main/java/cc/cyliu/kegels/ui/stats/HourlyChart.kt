package cc.cyliu.kegels.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cc.cyliu.kegels.R

@Composable
fun HourlyChart(
    hourlyDistribution: List<Int>,
    modifier: Modifier = Modifier
) {
    val data = if (hourlyDistribution.size == 24) hourlyDistribution
               else List(24) { hourlyDistribution.getOrElse(it) { 0 } }

    if (data.all { it == 0 }) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.stats_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val barColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val axisColor = MaterialTheme.colorScheme.outline
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val yLabelWidth = 32.dp.toPx()
        val xLabelHeight = 20.dp.toPx()
        val topPad = 18.dp.toPx() // room for count labels above bars

        val chartLeft = yLabelWidth
        val chartRight = size.width
        val chartTop = topPad
        val chartBottom = size.height - xLabelHeight
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        val maxVal = data.max().coerceAtLeast(1)
        val slotW = chartWidth / 24f
        val barW = 8.dp.toPx()

        // Y-axis grid lines at 0, 50%, 100%
        listOf(0, maxVal / 2, maxVal).forEach { value ->
            val y = chartBottom - (value.toFloat() / maxVal) * chartHeight
            drawLine(color = gridColor, start = Offset(chartLeft, y), end = Offset(chartRight, y), strokeWidth = 1.dp.toPx())
            val r = textMeasurer.measure(value.toString(), TextStyle(fontSize = 10.sp, color = labelColor))
            drawText(r, topLeft = Offset(0f, y - r.size.height / 2f))
        }

        // X-axis baseline
        drawLine(color = axisColor, start = Offset(chartLeft, chartBottom), end = Offset(chartRight, chartBottom), strokeWidth = 1.dp.toPx())

        data.forEachIndexed { hour, count ->
            val slotLeft = chartLeft + hour * slotW
            val barLeft = slotLeft + (slotW - barW) / 2f
            val barH = (count.toFloat() / maxVal) * chartHeight
            val barTop = chartBottom - barH

            if (count > 0) {
                // Bar
                drawRect(color = barColor, topLeft = Offset(barLeft, barTop), size = Size(barW, barH))

                // Count above bar (9sp to fit 24 narrow slots)
                val cr = textMeasurer.measure(count.toString(), TextStyle(fontSize = 9.sp, color = barColor))
                val cx = (slotLeft + slotW / 2f - cr.size.width / 2f).coerceIn(chartLeft, chartRight - cr.size.width)
                drawText(cr, topLeft = Offset(cx, barTop - cr.size.height - 1.dp.toPx()))
            }
        }

        // X labels at 0, 6, 12, 18
        listOf(0, 6, 12, 18).forEach { hour ->
            val x = chartLeft + hour * slotW
            val r = textMeasurer.measure("$hour", TextStyle(fontSize = 10.sp, color = labelColor))
            drawText(r, topLeft = Offset(x, chartBottom + (xLabelHeight - r.size.height) / 2f))
        }
    }
}
