package com.example.scrolljourney.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrolljourney.domain.ui.AppMetric
import com.example.scrolljourney.domain.ui.DashboardUiState
import com.example.scrolljourney.ui.components.NeoBadge
import com.example.scrolljourney.ui.components.NeoButton
import com.example.scrolljourney.ui.components.NeoInsetBlock
import com.example.scrolljourney.ui.components.NeoPanel
import com.example.scrolljourney.ui.components.NeoTopBar
import com.example.scrolljourney.ui.theme.LocalScrollJourneyColors
import com.example.scrolljourney.ui.theme.ScrollJourneyTheme

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onNavigateToTracking: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToCalibration: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DashboardTopBar(onNavigateToPrivacy)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                PrimaryMetricCard(state)
            }

            item {
                QuickActionButtons(
                    isTrackingActive = state.isTrackingActive,
                    onNavigateToTracking = onNavigateToTracking,
                    onNavigateToStats = onNavigateToStats,
                    onNavigateToAchievements = onNavigateToAchievements
                )
            }

            item {
                XpProgressCard(state)
            }

            item {
                StreakCard(state)
            }

            item {
                if (state.topApps.isNotEmpty()) {
                    TopAppsCard(state.topApps)
                }
            }

            item {
                CalibrationQuickLink(onNavigateToCalibration)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DashboardTopBar(
    onNavigateToPrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeoTopBar(
        title = "ScrollJourney",
        fillColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
        trailing = {
            IconButton(onClick = onNavigateToPrivacy) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

@Composable
private fun PrimaryMetricCard(
    state: DashboardUiState,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Your thumb travelled",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "%.2f km".format(state.totalDistanceMeters / 1000.0),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "(Estimated scroll distance)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            NeoInsetBlock(
                fillColor = extra.reward,
                contentColor = extra.onReward,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.totalScrolls.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = extra.onReward
                        )
                        Text(
                            text = "Scrolls",
                            fontSize = 12.sp,
                            color = extra.onReward.copy(alpha = 0.7f),
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        NeoBadge(
                            text = if (state.isTrackingActive) "ON" else "OFF",
                            fillColor = if (state.isTrackingActive) extra.success else extra.borderInk,
                            contentColor = if (state.isTrackingActive) extra.onSuccess else MaterialTheme.colorScheme.background,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tracking",
                            fontSize = 12.sp,
                            color = extra.onReward.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButtons(
    isTrackingActive: Boolean,
    onNavigateToTracking: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NeoButton(
            onClick = onNavigateToTracking,
            fillColor = if (isTrackingActive) extra.success else MaterialTheme.colorScheme.tertiary,
            contentColor = if (isTrackingActive) extra.onSuccess else MaterialTheme.colorScheme.onTertiary,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Text(
                text = if (isTrackingActive) "Tracking ON" else "Enable",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        NeoButton(
            onClick = onNavigateToStats,
            fillColor = extra.reward,
            contentColor = extra.onReward,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                modifier = Modifier.height(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Stats", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        NeoButton(
            onClick = onNavigateToAchievements,
            fillColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Text("Achievements", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun XpProgressCard(
    state: DashboardUiState,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Level ${state.currentLevel}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            NeoBadge(
                text = "${state.currentXp} XP",
                fillColor = extra.reward,
                contentColor = extra.onReward
            )
        }

        LinearProgressIndicator(
            progress = { state.xpProgressPercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = extra.reward,
            trackColor = extra.borderInk.copy(alpha = 0.15f)
        )

        Text(
            text = "${state.xpToNextLevel} XP to next level",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun StreakCard(
    state: DashboardUiState,
    modifier: Modifier = Modifier
) {
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🔥",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = state.currentStreak.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary
                )
                Text(
                    text = "Day Streak",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "⭐",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = state.streakLongestDays.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary
                )
                Text(
                    text = "Best Streak",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun TopAppsCard(
    topApps: List<AppMetric>,
    modifier: Modifier = Modifier
) {
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Top Apps Today",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        topApps.take(3).forEachIndexed { index, app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${index + 1}. ${app.appName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                    Text(
                        text = "%.1f km • ${app.scrollCount} scrolls".format(app.distanceMeters / 1000.0),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CalibrationQuickLink(
    onNavigateToCalibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.height(24.dp)
                )

                Column {
                    Text(
                        text = "Calibration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Improve accuracy",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    )
                }
            }

            NeoButton(
                onClick = onNavigateToCalibration,
                fillColor = extra.reward,
                contentColor = extra.onReward,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Start", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    ScrollJourneyTheme {
        DashboardScreen(
            state = DashboardUiState(
                totalDistanceMeters = 4821.5,
                totalScrolls = 156,
                currentXp = 3500,
                currentLevel = 3,
                xpToNextLevel = 500,
                xpProgressPercent = 0.7f,
                currentStreak = 7,
                streakLongestDays = 14,
                topApps = listOf(
                    AppMetric("com.google.android.youtube", "YouTube", 2500.0, 89),
                    AppMetric("com.reddit.frontpage", "Reddit", 1200.0, 45),
                    AppMetric("com.twitter.android", "X", 1121.5, 22)
                ),
                isTrackingActive = true
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenEmptyPreview() {
    ScrollJourneyTheme {
        DashboardScreen(
            state = DashboardUiState(
                isTrackingActive = false
            )
        )
    }
}
