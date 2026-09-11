package com.example.scrolljourney.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.scrolljourney.domain.ui.NavigationEvent
import com.example.scrolljourney.ui.components.NeoBottomNavBar
import com.example.scrolljourney.ui.screens.AchievementsScreen
import com.example.scrolljourney.ui.screens.CalibrationScreen
import com.example.scrolljourney.ui.screens.DashboardScreen
import com.example.scrolljourney.ui.screens.PrivacyScreen
import com.example.scrolljourney.ui.screens.StatsScreen
import com.example.scrolljourney.ui.screens.TrackingScreen
import com.example.scrolljourney.ui.state.ScrollJourneyViewModel
import com.example.scrolljourney.domain.ui.StatsPeriod
import com.example.scrolljourney.ui.theme.LocalScrollJourneyColors

enum class ScrollJourneyScreen {
    DASHBOARD,
    TRACKING,
    STATS,
    ACHIEVEMENTS,
    CALIBRATION,
    PRIVACY
}

@Composable
fun ScrollJourneyNavigation(
    viewModel: ScrollJourneyViewModel,
    onOpenAccessibilitySettings: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf(ScrollJourneyScreen.DASHBOARD) }

    val dashboardState by viewModel.dashboardState.collectAsState()
    val trackingState by viewModel.trackingState.collectAsState()
    val statsState by viewModel.statsState.collectAsState()
    val achievementsState by viewModel.achievementsState.collectAsState()
    val calibrationState by viewModel.calibrationState.collectAsState()
    val privacyState by viewModel.privacyState.collectAsState()

    val extra = LocalScrollJourneyColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(extra.cream)
    ) {
        // Main Screen Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            when (currentScreen) {
                ScrollJourneyScreen.DASHBOARD -> {
                    DashboardScreen(
                        state = dashboardState,
                        statsState = statsState,
                        onToggleTracking = { enabled -> viewModel.setTrackingEnabled(enabled) },
                        onNavigateToTracking = { currentScreen = ScrollJourneyScreen.TRACKING },
                        onNavigateToStats = { currentScreen = ScrollJourneyScreen.STATS },
                        onNavigateToAchievements = { currentScreen = ScrollJourneyScreen.ACHIEVEMENTS },
                        onNavigateToCalibration = { currentScreen = ScrollJourneyScreen.CALIBRATION },
                        onNavigateToPrivacy = { currentScreen = ScrollJourneyScreen.PRIVACY }
                    )
                }

                ScrollJourneyScreen.TRACKING -> {
                    TrackingScreen(
                        state = trackingState,
                        onToggleTracking = { enabled -> viewModel.setTrackingEnabled(enabled) },
                        onOpenAccessibilitySettings = {
                            viewModel.openAccessibilitySettings()
                            onOpenAccessibilitySettings()
                        },
                        onNavigateBack = { currentScreen = ScrollJourneyScreen.DASHBOARD }
                    )
                }

                ScrollJourneyScreen.STATS -> {
                    StatsScreen(
                        state = statsState,
                        onPeriodSelected = { period -> viewModel.selectStatsPeriod(period) },
                        onNavigateBack = { currentScreen = ScrollJourneyScreen.DASHBOARD }
                    )
                }

                ScrollJourneyScreen.ACHIEVEMENTS -> {
                    AchievementsScreen(
                        state = achievementsState,
                        onNavigateBack = { currentScreen = ScrollJourneyScreen.DASHBOARD }
                    )
                }

                ScrollJourneyScreen.CALIBRATION -> {
                    CalibrationScreen(
                        state = calibrationState,
                        onStartCalibration = { /* TODO: wire to viewModel */ },
                        onCancelCalibration = { /* TODO: wire to viewModel */ },
                        onNavigateBack = { currentScreen = ScrollJourneyScreen.DASHBOARD }
                    )
                }

                ScrollJourneyScreen.PRIVACY -> {
                    PrivacyScreen(
                        state = privacyState,
                        onNavigateBack = { currentScreen = ScrollJourneyScreen.DASHBOARD }
                    )
                }
            }
        }

        // Neo-Brutalist Bottom Navigation Bar (Home, Journey, Achievements)
        NeoBottomNavBar(
            currentScreen = currentScreen,
            onNavigateToHome = { currentScreen = ScrollJourneyScreen.DASHBOARD },
            onNavigateToJourney = { currentScreen = ScrollJourneyScreen.STATS },
            onNavigateToAchievements = { currentScreen = ScrollJourneyScreen.ACHIEVEMENTS }
        )
    }
}
