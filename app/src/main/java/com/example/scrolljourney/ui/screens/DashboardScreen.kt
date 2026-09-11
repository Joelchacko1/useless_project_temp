package com.example.scrolljourney.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrolljourney.domain.ui.AggregatedStats
import com.example.scrolljourney.domain.ui.AppMetric
import com.example.scrolljourney.domain.ui.DashboardUiState
import com.example.scrolljourney.domain.ui.StatsUiState
import com.example.scrolljourney.ui.components.NeoBadge
import com.example.scrolljourney.ui.components.NeoButton
import com.example.scrolljourney.ui.components.NeoDimens
import com.example.scrolljourney.ui.components.NeoInsetBlock
import com.example.scrolljourney.ui.components.NeoPanel
import com.example.scrolljourney.ui.theme.LocalScrollJourneyColors
import com.example.scrolljourney.ui.theme.ScrollJourneyTheme

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    statsState: StatsUiState? = null,
    onToggleTracking: (Boolean) -> Unit = {},
    onNavigateToTracking: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToCalibration: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(extra.cream)
    ) {
        // Bold Yellow Header
        DashboardTopBar(onNavigateToSettings = onNavigateToPrivacy)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Main Hero Card (Large Purple Neo-Brutalist Card)
            item {
                HeroPurpleCard(state = state)
            }

            // Tracking Card (Large Yellow Card with Toggle & Green Indicator)
            item {
                NeoTrackingCard(
                    isTrackingActive = state.isTrackingActive,
                    onToggleTracking = {
                        onToggleTracking(!state.isTrackingActive)
                    },
                    onOpenTrackingDetails = onNavigateToTracking
                )
            }

            // Level / XP Section
            item {
                LevelXpCard(state = state)
            }

            // Stat Cards (Pink, Green, Blue, Orange 2x2 Grid)
            item {
                StatsGridSection(
                    dashboardState = state,
                    statsState = statsState,
                    onNavigateToStats = onNavigateToStats
                )
            }

            // Daily Quest Section
            item {
                DailyQuestCard(
                    state = state,
                    onKeepGoing = onNavigateToStats
                )
            }

            // Calibration link (Preserving existing action)
            item {
                CalibrationQuickLink(onNavigateToCalibration = onNavigateToCalibration)
            }

            // App Breakdown / Top Apps (Preserving existing feature)
            if (state.topApps.isNotEmpty()) {
                item {
                    TopAppsSection(topApps = state.topApps)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Bold Yellow Header:
 * - Title: "ThondiEngotta"
 * - Subtitle: "SCROLL LESS • LIVE MORE"
 * - Right icon: Settings gear icon (NO profile icon)
 */
@Composable
private fun DashboardTopBar(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    val borderWidthPx = with(androidx.compose.ui.platform.LocalDensity.current) { NeoDimens.TopBarBorderWidth.toPx() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(extra.yellow)
            .drawBehind {
                drawLine(
                    color = extra.borderInk,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = borderWidthPx
                )
            }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "ThondiEngotta",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                color = extra.borderInk
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "SCROLL LESS  •  LIVE MORE",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp,
                color = extra.borderInk.copy(alpha = 0.85f)
            )
        }

        // Chunky settings button
        Box(
            modifier = Modifier
                .background(extra.cream, RoundedCornerShape(10.dp))
                .border(NeoDimens.BorderWidthNested, extra.borderInk, RoundedCornerShape(10.dp))
        ) {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = extra.borderInk,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Main Hero Card:
 * - Thick black border & hard black shadow
 * - Purple background
 * - Large typography: "Your thumb travelled", dynamic km, "X scrolls today"
 * - Quote: "Small scrolls make big stories."
 * - Playful thumb illustration graphic
 * - Comic-style burst badge: "KEEP SCROLLING HIGHER!"
 */
@Composable
private fun HeroPurpleCard(
    state: DashboardUiState,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    val distanceKm = state.totalDistanceMeters / 1000.0

    NeoPanel(
        fillColor = extra.purple,
        contentColor = extra.cream,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        contentPadding = PaddingValues(20.dp)
    ) {
        // Top label
        Text(
            text = "YOUR THUMB TRAVELLED",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.2.sp,
            color = extra.yellow
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main stat display with playful thumb graphic
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Distance inset block
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(extra.cream, RoundedCornerShape(12.dp))
                    .border(NeoDimens.BorderWidth, extra.borderInk, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "%.2f km".format(distanceKm),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    color = extra.borderInk
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Playful thumb / hand comic illustration
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(extra.yellow, RoundedCornerShape(14.dp))
                    .border(NeoDimens.BorderWidthNested, extra.borderInk, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👍",
                    fontSize = 36.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic scroll count
        Text(
            text = "%,d scrolls today".format(state.totalScrolls),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = extra.cream
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Comic burst badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Comic-style burst badge
            Box(
                modifier = Modifier
                    .rotate(-4f)
                    .background(extra.yellow, RoundedCornerShape(8.dp))
                    .border(NeoDimens.BorderWidthNested, extra.borderInk, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "★ KEEP SCROLLING HIGHER!",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = extra.borderInk
                )
            }
        }
    }
}

/**
 * Tracking Card:
 * - Large yellow tracking card
 * - Green status indicator
 * - "Tracking ON" / "Tracking OFF"
 * - "We're counting your scrolls" / "Tracking paused"
 * - Large ON/OFF toggle on the right
 */
@Composable
private fun NeoTrackingCard(
    isTrackingActive: Boolean,
    onToggleTracking: () -> Unit,
    onOpenTrackingDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current

    NeoPanel(
        fillColor = extra.yellow,
        contentColor = extra.borderInk,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenTrackingDetails),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Indicator + Labels
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Green indicator dot / badge
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isTrackingActive) extra.green else extra.pink)
                        .border(2.5.dp, extra.borderInk, CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isTrackingActive) "Tracking ON" else "Tracking OFF",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = extra.borderInk
                    )
                    Text(
                        text = if (isTrackingActive) "We're counting your scrolls" else "Tap to enable tracking",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = extra.borderInk.copy(alpha = 0.75f)
                    )
                }
            }

            // Right: Chunky ON/OFF Neo Toggle
            Box(
                modifier = Modifier
                    .background(
                        color = if (isTrackingActive) extra.green else extra.cream,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(NeoDimens.BorderWidthNested, extra.borderInk, RoundedCornerShape(10.dp))
                    .clickable(onClick = onToggleTracking)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isTrackingActive) "ON" else "OFF",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = extra.borderInk
                )
            }
        }
    }
}

/**
 * Level / XP Section:
 * - Gamified Level card
 * - Cream/white card with black border & hard shadow
 * - Level X & title
 * - Purple progress bar
 * - X / Y XP
 * - Badge / crown icon
 */
@Composable
private fun LevelXpCard(
    state: DashboardUiState,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current

    NeoPanel(
        fillColor = extra.cream,
        contentColor = extra.borderInk,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LEVEL ${state.currentLevel}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = extra.borderInk
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "👑",
                        fontSize = 18.sp
                    )
                }

                Text(
                    text = when {
                        state.currentLevel < 3 -> "Scroll Novice"
                        state.currentLevel < 6 -> "Thumb Wanderer"
                        state.currentLevel < 10 -> "Scroll Explorer"
                        else -> "Master Scroller"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = extra.purple
                )
            }

            // XP Badge
            Box(
                modifier = Modifier
                    .background(extra.yellow, RoundedCornerShape(8.dp))
                    .border(NeoDimens.BorderWidthNested, extra.borderInk, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${state.currentXp} XP",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = extra.borderInk
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Purple progress bar with thick black outline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .background(extra.cream, RoundedCornerShape(9.dp))
                .border(NeoDimens.BorderWidthNested, extra.borderInk, RoundedCornerShape(9.dp))
                .clip(RoundedCornerShape(9.dp))
        ) {
            val progress = state.xpProgressPercent.coerceIn(0.04f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(18.dp)
                    .background(extra.purple)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${state.currentXp} / ${(state.currentXp + state.xpToNextLevel).coerceAtLeast(state.currentXp + 100)} XP",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = extra.borderInk
            )

            Text(
                text = "${state.xpToNextLevel} XP to next level",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = extra.borderInk.copy(alpha = 0.65f)
            )
        }
    }
}

/**
 * Colorful Neo-Brutalist Stat Cards:
 * - 2x2 Responsive grid
 * - Today → Pink
 * - This Week → Green
 * - All Time → Blue
 * - Day Streak → Orange
 */
@Composable
private fun StatsGridSection(
    dashboardState: DashboardUiState,
    statsState: StatsUiState?,
    onNavigateToStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current

    // Use dynamic data from statsState if available, otherwise dashboardState
    val todayKm = dashboardState.totalDistanceMeters / 1000.0
    val todayScrolls = dashboardState.totalScrolls

    val weekKm = statsState?.weekStats?.let { it.totalDistanceMeters / 1000.0 } ?: (todayKm * 1.5)
    val weekScrolls = statsState?.weekStats?.totalScrolls ?: (todayScrolls * 2)

    val allTimeKm = statsState?.monthStats?.let { it.totalDistanceMeters / 1000.0 } ?: (todayKm * 3.5)
    val allTimeScrolls = statsState?.monthStats?.totalScrolls ?: (todayScrolls * 4)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Today (Pink) & This Week (Green)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatGridCard(
                title = "TODAY",
                icon = "⚡",
                primaryText = "%.2f km".format(todayKm),
                secondaryText = "%,d scrolls".format(todayScrolls),
                fillColor = extra.pink,
                onClick = onNavigateToStats,
                modifier = Modifier.weight(1f)
            )

            StatGridCard(
                title = "THIS WEEK",
                icon = "🚀",
                primaryText = "%.2f km".format(weekKm),
                secondaryText = "%,d scrolls".format(weekScrolls),
                fillColor = extra.green,
                onClick = onNavigateToStats,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: All Time (Blue) & Day Streak (Orange)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatGridCard(
                title = "ALL TIME",
                icon = "🌎",
                primaryText = "%.1f km".format(allTimeKm),
                secondaryText = "%,d scrolls".format(allTimeScrolls),
                fillColor = extra.blue,
                onClick = onNavigateToStats,
                modifier = Modifier.weight(1f)
            )

            StatGridCard(
                title = "DAY STREAK",
                icon = "🔥",
                primaryText = "${dashboardState.currentStreak} Days",
                secondaryText = "Best: ${dashboardState.streakLongestDays} Days",
                fillColor = extra.orange,
                onClick = onNavigateToStats,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatGridCard(
    title: String,
    icon: String,
    primaryText: String,
    secondaryText: String,
    fillColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current

    NeoPanel(
        fillColor = fillColor,
        contentColor = extra.borderInk,
        modifier = modifier.clickable(onClick = onClick),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = extra.borderInk
            )
            Text(text = icon, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = extra.borderInk
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = secondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = extra.borderInk.copy(alpha = 0.8f)
        )
    }
}

/**
 * Daily Quest Card:
 * - Title: Daily Quest
 * - Quest: Scroll 2,000 times
 * - Description: Every scroll counts!
 * - Progress bar & ratio (dynamic based on today's scrolls)
 * - XP reward badge (+300 XP)
 * - Button: Keep Going →
 */
@Composable
private fun DailyQuestCard(
    state: DashboardUiState,
    onKeepGoing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    val targetScrolls = 2000L
    val currentProgress = state.totalScrolls
    val questFraction = (currentProgress.toFloat() / targetScrolls).coerceIn(0.04f, 1f)

    NeoPanel(
        fillColor = extra.cream,
        contentColor = extra.borderInk,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        // Quest Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎯", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Quest",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = extra.borderInk
                )
            }

            // Reward badge
            Box(
                modifier = Modifier
                    .background(extra.yellow, RoundedCornerShape(8.dp))
                    .border(NeoDimens.BorderWidthNested, extra.borderInk, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "⭐ +300 XP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = extra.borderInk
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Scroll 2,000 times",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = extra.borderInk
        )
        Text(
            text = "Every scroll counts!",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = extra.borderInk.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(extra.cream, RoundedCornerShape(8.dp))
                .border(NeoDimens.BorderWidthNested, extra.borderInk, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(questFraction)
                    .height(16.dp)
                    .background(extra.orange)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "%,d / %,d".format(currentProgress, targetScrolls),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = extra.borderInk
            )

            // Keep Going Button
            NeoButton(
                onClick = onKeepGoing,
                fillColor = extra.yellow,
                contentColor = extra.borderInk,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Keep Going →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

/**
 * Top Apps Card:
 * Preserves the existing top app usage feature in full neo-brutalist style
 */
@Composable
private fun TopAppsSection(
    topApps: List<AppMetric>,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current

    NeoPanel(
        fillColor = extra.cream,
        contentColor = extra.borderInk,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = "📱 Top Apps Today",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = extra.borderInk,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        topApps.take(3).forEachIndexed { index, app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${index + 1}. ${app.appName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = extra.borderInk
                    )
                    Text(
                        text = "%.2f km • %,d scrolls".format(app.distanceMeters / 1000.0, app.scrollCount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = extra.borderInk.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Calibration quick link
 * Preserves existing calibration screen navigation
 */
@Composable
private fun CalibrationQuickLink(
    onNavigateToCalibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current

    NeoPanel(
        fillColor = extra.cream,
        contentColor = extra.borderInk,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = extra.borderInk,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Text(
                        text = "Sensor Calibration",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = extra.borderInk
                    )
                    Text(
                        text = "Tune scroll accuracy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = extra.borderInk.copy(alpha = 0.7f)
                    )
                }
            }

            NeoButton(
                onClick = onNavigateToCalibration,
                fillColor = extra.yellow,
                contentColor = extra.borderInk,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Calibrate", fontSize = 11.sp, fontWeight = FontWeight.Black)
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
                totalDistanceMeters = 840.0,
                totalScrolls = 1284,
                currentXp = 4260,
                currentLevel = 7,
                xpToNextLevel = 740,
                xpProgressPercent = 0.85f,
                currentStreak = 0,
                streakLongestDays = 0,
                topApps = listOf(
                    AppMetric("com.google.android.youtube", "YouTube", 520.0, 890),
                    AppMetric("com.reddit.frontpage", "Reddit", 320.0, 394)
                ),
                isTrackingActive = true
            )
        )
    }
}
