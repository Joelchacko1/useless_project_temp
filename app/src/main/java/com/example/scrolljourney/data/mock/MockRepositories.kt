package com.example.scrolljourney.data.mock

import com.example.scrolljourney.domain.repository.CalibrationRepository
import com.example.scrolljourney.domain.repository.GamificationRepository
import com.example.scrolljourney.domain.repository.ScrollStatsRepository
import com.example.scrolljourney.domain.repository.TrackingController
import com.example.scrolljourney.domain.repository.AchievementData
import com.example.scrolljourney.domain.ui.AggregatedStats
import com.example.scrolljourney.domain.ui.AppMetric
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Mock implementations of repository interfaces for testing and demo purposes.
 * These will be replaced with real implementations from Agent 3 and Agent 4.
 */

class MockScrollStatsRepository : ScrollStatsRepository {
    override fun observeToday(): Flow<AggregatedStats> {
        return flowOf(
            AggregatedStats(
                totalScrolls = 42,
                totalDistanceMeters = 1234.5,
                activeTrackingMs = 180000,
                dateKey = "2026-09-10",
                topApps = listOf(
                    AppMetric("com.google.android.youtube", "YouTube", 750.0, 28),
                    AppMetric("com.reddit.frontpage", "Reddit", 350.0, 12),
                    AppMetric("com.twitter.android", "X", 134.5, 2)
                )
            )
        )
    }

    override fun observeWeek(): Flow<AggregatedStats> {
        return flowOf(
            AggregatedStats(
                totalScrolls = 245,
                totalDistanceMeters = 8942.5,
                activeTrackingMs = 1200000
            )
        )
    }

    override fun observeMonth(): Flow<AggregatedStats> {
        return flowOf(
            AggregatedStats(
                totalScrolls = 892,
                totalDistanceMeters = 35680.0,
                activeTrackingMs = 4800000
            )
        )
    }

    override fun observeAppBreakdown(dateKey: String): Flow<List<AppMetric>> {
        return flowOf(
            listOf(
                AppMetric("com.google.android.youtube", "YouTube", 750.0, 28),
                AppMetric("com.reddit.frontpage", "Reddit", 350.0, 12),
                AppMetric("com.twitter.android", "X", 134.5, 2)
            )
        )
    }
}

class MockTrackingController : TrackingController {
    override fun isTrackingEnabled(): Flow<Boolean> = flowOf(true)

    override suspend fun setTrackingEnabled(enabled: Boolean) {
        // Mock implementation
    }

    override fun openAccessibilitySettings() {
        // Mock implementation
    }

    override fun isServiceConnected(): Flow<Boolean> = flowOf(true)

    override fun isAccessibilityPermissionGranted(): Flow<Boolean> = flowOf(true)
}

class MockGamificationRepository : GamificationRepository {
    override fun observeCurrentXpAndLevel(): Flow<Pair<Long, Int>> {
        return flowOf(3500L to 3)
    }

    override fun observeCurrentStreak(): Flow<Int> = flowOf(7)

    override fun observeLongestStreak(): Flow<Int> = flowOf(14)

    override fun observeAchievements(): Flow<List<AchievementData>> {
        return flowOf(
            listOf(
                AchievementData(
                    id = "first_scroll",
                    title = "First Scroll",
                    description = "Record your first scroll",
                    isUnlocked = true,
                    xpReward = 10
                ),
                AchievementData(
                    id = "one_km",
                    title = "One Kilometer",
                    description = "Scroll 1 km in total",
                    isUnlocked = true,
                    xpReward = 50
                ),
                AchievementData(
                    id = "seven_day_streak",
                    title = "Week Warrior",
                    description = "7 day streak",
                    progress = 3,
                    progressMax = 7,
                    isUnlocked = false,
                    xpReward = 100
                ),
                AchievementData(
                    id = "ten_km",
                    title = "Ten Kilometers",
                    description = "Scroll 10 km in total",
                    progress = 5,
                    progressMax = 10,
                    isUnlocked = false,
                    xpReward = 200
                )
            )
        )
    }

    override fun observeXpToNextLevel(): Flow<Long> = flowOf(500L)
}

class MockCalibrationRepository : CalibrationRepository {
    override suspend fun startCalibration(): String = "calib_001"

    override suspend fun cancelCalibration() {
        // Mock implementation
    }

    override fun observeCalibrationProgress(calibrationId: String): Flow<CalibrationRepository.CalibrationProgress> {
        return flowOf(
            CalibrationRepository.CalibrationProgress(
                calibrationId = calibrationId,
                sampleCount = 0,
                targetSampleCount = 20,
                medianDistanceMeters = 0.0,
                meanDistanceMeters = 0.0,
                isCompleted = false
            )
        )
    }
}
