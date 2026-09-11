package com.example.scrolljourney.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrolljourney.domain.ui.AppMetric
import com.example.scrolljourney.domain.ui.AggregatedStats
import com.example.scrolljourney.domain.ui.StatsPeriod
import com.example.scrolljourney.domain.ui.StatsUiState
import com.example.scrolljourney.ui.components.NeoInsetBlock
import com.example.scrolljourney.ui.components.NeoPanel
import com.example.scrolljourney.ui.components.NeoTopBar
import com.example.scrolljourney.ui.theme.ScrollJourneyTheme

@Composable
fun StatsScreen(
    state: StatsUiState,
    onPeriodSelected: (StatsPeriod) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NeoTopBar(
            title = "Statistics",
            fillColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                PeriodSelector(
                    selectedPeriod = state.selectedPeriod,
                    onPeriodSelected = onPeriodSelected
                )
            }

            item {
                val currentStats = when (state.selectedPeriod) {
                    StatsPeriod.TODAY -> state.todayStats
                    StatsPeriod.WEEK -> state.weekStats
                    StatsPeriod.MONTH -> state.monthStats
                }

                PeriodStatsCard(stats = currentStats, period = state.selectedPeriod)
            }

            item {
                val topApps = when (state.selectedPeriod) {
                    StatsPeriod.TODAY -> state.todayStats.topApps
                    StatsPeriod.WEEK -> state.weekStats.topApps
                    StatsPeriod.MONTH -> state.monthStats.topApps
                }

                if (topApps.isNotEmpty()) {
                    TopAppsForPeriodCard(topApps, state.selectedPeriod)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            StatsPeriod.values().forEach { period ->
                SegmentedButton(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodSelected(period) },
                    shape = RoundedCornerShape(4.dp),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary,
                        activeContentColor = MaterialTheme.colorScheme.onPrimary,
                        activeBorderColor = MaterialTheme.colorScheme.outline,
                        inactiveContainerColor = MaterialTheme.colorScheme.background,
                        inactiveContentColor = MaterialTheme.colorScheme.onBackground,
                        inactiveBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = when (period) {
                            StatsPeriod.TODAY -> "Today"
                            StatsPeriod.WEEK -> "Week"
                            StatsPeriod.MONTH -> "Month"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodStatsCard(
    stats: AggregatedStats,
    period: StatsPeriod,
    modifier: Modifier = Modifier
) {
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(24.dp)
    ) {
        Text(
            text = when (period) {
                StatsPeriod.TODAY -> "Today's Stats"
                StatsPeriod.WEEK -> "This Week"
                StatsPeriod.MONTH -> "This Month"
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Estimated Distance",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "%.2f km".format(stats.totalDistanceMeters / 1000.0),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        NeoInsetBlock(
            fillColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stats.totalScrolls.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                    Text(
                        text = "Scrolls",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = (stats.activeTrackingMs / 60000).toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                    Text(
                        text = "Minutes",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (stats.totalScrolls > 0) {
                            (stats.totalDistanceMeters / stats.totalScrolls).toInt().toString()
                        } else {
                            "0"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                    Text(
                        text = "Avg (m)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TopAppsForPeriodCard(
    topApps: List<AppMetric>,
    period: StatsPeriod,
    modifier: Modifier = Modifier
) {
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Top Apps",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val shown = topApps.take(5)
        shown.forEachIndexed { index, app ->
            AppStatsRow(app, index + 1)
            if (index < shown.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(vertical = 8.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun AppStatsRow(
    app: AppMetric,
    rank: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$rank. ${app.appName}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = app.packageName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }

            Text(
                text = "%.1f km".format(app.distanceMeters / 1000.0),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${app.scrollCount} scrolls",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatsScreenPreview() {
    ScrollJourneyTheme {
        StatsScreen(
            state = StatsUiState(
                selectedPeriod = StatsPeriod.TODAY,
                todayStats = AggregatedStats(
                    totalScrolls = 156,
                    totalDistanceMeters = 4821.5,
                    activeTrackingMs = 120000,
                    topApps = listOf(
                        AppMetric("com.google.android.youtube", "YouTube", 2500.0, 89),
                        AppMetric("com.reddit.frontpage", "Reddit", 1200.0, 45),
                        AppMetric("com.twitter.android", "X", 1121.5, 22)
                    )
                ),
                weekStats = AggregatedStats(
                    totalScrolls = 892,
                    totalDistanceMeters = 28150.0,
                    activeTrackingMs = 720000
                ),
                monthStats = AggregatedStats(
                    totalScrolls = 3245,
                    totalDistanceMeters = 102350.0,
                    activeTrackingMs = 3000000
                )
            )
        )
    }
}
