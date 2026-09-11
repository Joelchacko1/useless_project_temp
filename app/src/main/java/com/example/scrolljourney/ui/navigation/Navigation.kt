package com.example.scrolljourney.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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

    // Covers both permission prompts below: reset true on every app entry (cold start or
    // returning from background), so a "Not now" only hides them for the current visit.
    var showPermissionPrompt by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                showPermissionPrompt = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showPermissionPrompt && !trackingState.isAccessibilityPermissionGranted) {
        AccessibilityPermissionDialog(
            onOpenSettings = {
                viewModel.openAccessibilitySettings()
                onOpenAccessibilitySettings()
                showPermissionPrompt = false
            },
            onDismiss = { showPermissionPrompt = false },
        )
    } else if (showPermissionPrompt && !trackingState.isOverlayPermissionGranted) {
        OverlayPermissionDialog(
            onOpenSettings = {
                viewModel.openOverlaySettings()
                showPermissionPrompt = false
            },
            onDismiss = { showPermissionPrompt = false },
        )
    }

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

@Composable
private fun AccessibilityPermissionDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Accessibility Access") },
        text = {
            Text(
                "ScrollJourney needs Accessibility permission to detect your scrolls. This is " +
                    "used only to observe scroll events — never to read screen content, typed " +
                    "text, or passwords. Android requires you to turn this on yourself in " +
                    "Settings; tap below to go straight there.",
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) { Text("Open Accessibility Settings") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Not now") }
        },
    )
}

@Composable
private fun OverlayPermissionDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Show Achievements Over Other Apps") },
        text = {
            Text(
                "To celebrate an achievement the moment it unlocks — even while you're using " +
                    "another app — ScrollJourney needs the \"draw over other apps\" permission. " +
                    "Android requires you to turn this on yourself in Settings; tap below to go " +
                    "straight there.",
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) { Text("Open Settings") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Not now") }
        },
    )
}
