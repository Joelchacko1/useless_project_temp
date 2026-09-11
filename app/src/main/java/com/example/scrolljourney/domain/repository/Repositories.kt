package com.example.scrolljourney.domain.repository

import com.example.scrolljourney.domain.ui.AppMetric
import com.example.scrolljourney.domain.ui.AggregatedStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for scroll statistics.
 * Provides aggregated scroll data for different time periods.
 * Implemented by Agent 3 (Data/Gamification layer).
 */
interface ScrollStatsRepository {
    /**
     * Observe today's aggregated statistics.
     */
    fun observeToday(): Flow<AggregatedStats>

    /**
     * Observe this week's aggregated statistics.
     */
    fun observeWeek(): Flow<AggregatedStats>

    /**
     * Observe this month's aggregated statistics.
     */
    fun observeMonth(): Flow<AggregatedStats>

    /**
     * Observe app-level breakdown for a specific date key (format: YYYY-MM-DD).
     */
    fun observeAppBreakdown(dateKey: String): Flow<List<AppMetric>>
}

/**
 * Repository interface for tracking control.
 * Manages tracking ON/OFF state and accessibility service interactions.
 * Implemented by Agent 4 (Integration layer).
 */
interface TrackingController {
    /**
     * Observe whether tracking is currently enabled.
     */
    fun isTrackingEnabled(): Flow<Boolean>

    /**
     * Set tracking enabled/disabled state.
     */
    suspend fun setTrackingEnabled(enabled: Boolean)

    /**
     * Open Android Accessibility Settings for the ScrollJourney service.
     */
    fun openAccessibilitySettings()

    /**
     * Observe whether the accessibility service is connected.
     */
    fun isServiceConnected(): Flow<Boolean>

    /**
     * Observe whether accessibility permission is granted.
     */
    fun isAccessibilityPermissionGranted(): Flow<Boolean>
}

/**
 * Repository interface for gamification data.
 * Provides achievements, XP levels, and streaks.
 * Implemented by Agent 3 (Gamification layer).
 */
interface GamificationRepository {
    /**
     * Observe current XP and level.
     */
    fun observeCurrentXpAndLevel(): Flow<Pair<Long, Int>>

    /**
     * Observe current streak.
     */
    fun observeCurrentStreak(): Flow<Int>

    /**
     * Observe longest streak.
     */
    fun observeLongestStreak(): Flow<Int>

    /**
     * Observe all achievements.
     */
    fun observeAchievements(): Flow<List<AchievementData>>

    /**
     * Observe XP needed to reach the next level.
     */
    fun observeXpToNextLevel(): Flow<Long>
}

data class AchievementData(
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
 * Repository interface for calibration.
 * Manages calibration runs and results.
 * Implemented by Agent 3 (Data layer).
 */
interface CalibrationRepository {
    /**
     * Start a new calibration run.
     */
    suspend fun startCalibration(): String

    /**
     * Cancel ongoing calibration.
     */
    suspend fun cancelCalibration()

    /**
     * Observe calibration progress.
     */
    fun observeCalibrationProgress(calibrationId: String): Flow<CalibrationProgress>

    data class CalibrationProgress(
        val calibrationId: String,
        val sampleCount: Int,
        val targetSampleCount: Int,
        val medianDistanceMeters: Double,
        val meanDistanceMeters: Double,
        val isCompleted: Boolean,
        val error: String? = null
    )
}
