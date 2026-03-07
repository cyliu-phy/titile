package cc.cyliu.kegels.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.cyliu.kegels.R

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            val statItems = listOf(
                stringResource(R.string.stats_today) to uiState.todayTotal.toString(),
                stringResource(R.string.stats_this_week) to uiState.weekTotal.toString(),
                stringResource(R.string.stats_all_time) to uiState.allTimeTotal.toString(),
                stringResource(R.string.stats_streak) to "${uiState.streakDays} ${stringResource(R.string.stats_days_suffix)}"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(statItems) { (label, value) ->
                    StatCard(label = label, value = value)
                }
            }
        }

        item {
            ChartSection(title = stringResource(R.string.stats_heatmap)) {
                HeatmapGrid(
                    dailyTotals = uiState.dailyTotals,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            ChartSection(title = stringResource(R.string.stats_hourly)) {
                HourlyChart(
                    hourlyDistribution = uiState.hourlyDistribution,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }

        item {
            ChartSection(title = stringResource(R.string.stats_weekly)) {
                WeeklyChart(
                    weeklyTotals = uiState.weeklyTotals,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
        }
    }
}

@Composable
private fun ChartSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
