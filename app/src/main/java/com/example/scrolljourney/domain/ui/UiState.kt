package com.example.scrolljourney.domain.ui

import kotlinx.coroutines.flow.StateFlow

/**
 * UI state for the Dashboard screen.
 * Displays primary metrics: estimated scroll distance, XP/level, streak, top apps.
 */
data class DashboardUiState(
    val isLoading: Boolean = false,
    val totalDistanceMeters: Double = 0.0,
    val totalScrolls: Long = 0L,
    val currentXp: Long = 0L,
    val currentLevel: Int = 1,
    val xpToNextLevel: Long = 0L,
    val xpProgressPercent: Float = 0f,
    val currentStreak: Int = 0,
    val streakLongestDays: Int = 0,
    val topApps: List<AppMetric> = emptyList(),
    val isTrackingActive: Boolean = false,
    val error: String? = null
)

data class AppMetric(
    val packageName: String,
    val appName: String,
    val distanceMeters: Double,
    val scrollCount: Long,
    val iconUrl: String? = null
)

/**
 * UI state for the Tracking control screen.
 * Shows tracking ON/OFF state, permission status, and actions.
 */
data class TrackingUiState(
    val isTrackingEnabled: Boolean = false,
    val serviceConnected: Boolean = false,
    val isAccessibilityPermissionGranted: Boolean = false,
    val activeSinceEpochMs: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * UI state for the Calibration screen.
 * Tracks calibration progress and results.
 */
data class CalibrationUiState(
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,
    val sampleCount: Int = 0,
    val targetSampleCount: Int = 20,
    val medianDistanceMeters: Double = 0.0,
    val meanDistanceMeters: Double = 0.0,
    val progress: Float = 0f,
    val error: String? = null
)

/**
 * UI state for the Statistics screen.
 * Aggregated stats for different time periods.
 */
data class StatsUiState(
    val selectedPeriod: StatsPeriod = StatsPeriod.TODAY,
    val todayStats: AggregatedStats = AggregatedStats(),
    val weekStats: AggregatedStats = AggregatedStats(),
    val monthStats: AggregatedStats = AggregatedStats(),
    val appBreakdown: List<AppMetric> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class StatsPeriod {
    TODAY, WEEK, MONTH
}

data class AggregatedStats(
    val totalScrolls: Long = 0L,
    val totalDistanceMeters: Double = 0.0,
    val activeTrackingMs: Long = 0L,
    val dateKey: String = "",
    val topApps: List<AppMetric> = emptyList()
)

/**
 * UI state for the Achievements screen.
 * Displays achievements with locked/unlocked states.
 */
data class AchievementsUiState(
    val achievements: List<AchievementCard> = emptyList(),
    val totalAchievementsUnlocked: Int = 0,
    val unlockedPercentage: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AchievementCard(
    val id: String,
    val title: String,
    val description: String,
    val icon: String? = null,
    val isUnlocked: Boolean = false,
    val unlockedAtEpochMs: Long? = null,
    val xpReward: Long = 0L,
    val progress: Int = 0,
    val progressMax: Int = 100
)

/**
 * UI state for the Privacy/Settings screen.
 */
data class PrivacyUiState(
    val isLocalStorageOnly: Boolean = true,
    val collectedData: List<String> = listOf(
        "Event timestamp",
        "Source app package identifier",
        "Scroll-related metadata needed for distance estimation"
    ),
    val notCollectedData: List<String> = listOf(
        "Screenshots",
        "Screen contents",
        "Typed text",
        "Messages",
        "Passwords",
        "Post content"
    ),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * Navigation state for the app.
 */
sealed class NavigationEvent {
    data object NavigateToDashboard : NavigationEvent()
    data object NavigateToTracking : NavigationEvent()
    data object NavigateToCalibration : NavigationEvent()
    data object NavigateToStats : NavigationEvent()
    data object NavigateToAchievements : NavigationEvent()
    data object NavigateToPrivacy : NavigationEvent()
    data object OpenAccessibilitySettings : NavigationEvent()
    data object PopBackStack : NavigationEvent()
}
