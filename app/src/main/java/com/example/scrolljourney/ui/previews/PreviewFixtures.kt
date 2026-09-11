package com.example.scrolljourney.ui.previews

import com.example.scrolljourney.domain.ui.AchievementCard
import com.example.scrolljourney.domain.ui.AchievementsUiState
import com.example.scrolljourney.domain.ui.AggregatedStats
import com.example.scrolljourney.domain.ui.AppMetric
import com.example.scrolljourney.domain.ui.CalibrationUiState
import com.example.scrolljourney.domain.ui.DashboardUiState
import com.example.scrolljourney.domain.ui.PrivacyUiState
import com.example.scrolljourney.domain.ui.StatsUiState
import com.example.scrolljourney.domain.ui.StatsPeriod
import com.example.scrolljourney.domain.ui.TrackingUiState

/**
 * Fixture data for UI previews and testing.
 */

object DashboardPreviewFixtures {
    val populatedState = DashboardUiState(
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

    val emptyState = DashboardUiState(
        isTrackingActive = false
    )

    val largeValuesState = DashboardUiState(
        totalDistanceMeters = 123456.7,
        totalScrolls = 9999,
        currentXp = 999999,
        currentLevel = 50,
        xpToNextLevel = 1,
        xpProgressPercent = 0.95f,
        currentStreak = 365,
        streakLongestDays = 500,
        topApps = listOf(
            AppMetric("com.veryverylongapplicationname.withsubpackages", "Very Long App Name Here", 95000.0, 3456),
            AppMetric("com.app.b", "B", 50000.0, 1234),
            AppMetric("com.app.c", "C", 28456.7, 456)
        ),
        isTrackingActive = true
    )
}

object TrackingPreviewFixtures {
    val activeWithPermission = TrackingUiState(
        isTrackingEnabled = true,
        serviceConnected = true,
        isAccessibilityPermissionGranted = true
    )

    val enabledNeedPermission = TrackingUiState(
        isTrackingEnabled = false,
        serviceConnected = false,
        isAccessibilityPermissionGranted = false
    )

    val activeButNoService = TrackingUiState(
        isTrackingEnabled = true,
        serviceConnected = false,
        isAccessibilityPermissionGranted = true
    )
}

object StatsPreviewFixtures {
    val populatedStats = StatsUiState(
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

    val emptyStats = StatsUiState(
        todayStats = AggregatedStats()
    )
}

object AchievementsPreviewFixtures {
    val populatedAchievements = AchievementsUiState(
        achievements = listOf(
            AchievementCard(
                id = "first_scroll",
                title = "First Scroll",
                description = "Record your first scroll",
                isUnlocked = true,
                xpReward = 10
            ),
            AchievementCard(
                id = "one_km",
                title = "One Kilometer",
                description = "Scroll 1 km in total",
                isUnlocked = true,
                xpReward = 50
            ),
            AchievementCard(
                id = "seven_day_streak",
                title = "Week Warrior",
                description = "7 day streak",
                progress = 3,
                progressMax = 7,
                isUnlocked = false,
                xpReward = 100
            ),
            AchievementCard(
                id = "ten_km",
                title = "Ten Kilometers",
                description = "Scroll 10 km in total",
                progress = 5,
                progressMax = 10,
                isUnlocked = false,
                xpReward = 200
            ),
            AchievementCard(
                id = "thirty_km",
                title = "Thirty Kilometers",
                description = "Scroll 30 km in total",
                progress = 0,
                progressMax = 30,
                isUnlocked = false,
                xpReward = 500
            )
        ),
        totalAchievementsUnlocked = 2,
        unlockedPercentage = 40f
    )

    val allUnlocked = AchievementsUiState(
        achievements = listOf(
            AchievementCard(
                id = "first_scroll",
                title = "First Scroll",
                description = "Record your first scroll",
                isUnlocked = true,
                xpReward = 10
            ),
            AchievementCard(
                id = "one_km",
                title = "One Kilometer",
                description = "Scroll 1 km in total",
                isUnlocked = true,
                xpReward = 50
            ),
            AchievementCard(
                id = "ten_km",
                title = "Ten Kilometers",
                description = "Scroll 10 km in total",
                isUnlocked = true,
                xpReward = 200
            )
        ),
        totalAchievementsUnlocked = 3,
        unlockedPercentage = 100f
    )
}

object CalibrationPreviewFixtures {
    val idle = CalibrationUiState(
        isRunning = false,
        isCompleted = false,
        sampleCount = 0,
        targetSampleCount = 20
    )

    val running = CalibrationUiState(
        isRunning = true,
        isCompleted = false,
        sampleCount = 12,
        targetSampleCount = 20,
        progress = 0.6f
    )

    val completed = CalibrationUiState(
        isRunning = false,
        isCompleted = true,
        sampleCount = 20,
        targetSampleCount = 20,
        medianDistanceMeters = 0.85,
        meanDistanceMeters = 0.82,
        progress = 1.0f
    )

    val error = CalibrationUiState(
        isRunning = false,
        isCompleted = false,
        error = "Failed to connect to accessibility service. Please ensure ScrollJourney has permission and is running."
    )
}

object PrivacyPreviewFixtures {
    val default = PrivacyUiState()
}
