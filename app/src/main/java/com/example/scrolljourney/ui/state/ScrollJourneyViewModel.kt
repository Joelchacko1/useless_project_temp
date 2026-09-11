package com.example.scrolljourney.ui.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrolljourney.domain.repository.CalibrationRepository
import com.example.scrolljourney.domain.repository.GamificationRepository
import com.example.scrolljourney.domain.repository.ScrollStatsRepository
import com.example.scrolljourney.domain.repository.TrackingController
import com.example.scrolljourney.domain.ui.DashboardUiState
import com.example.scrolljourney.domain.ui.AppMetric
import com.example.scrolljourney.domain.ui.AchievementsUiState
import com.example.scrolljourney.domain.ui.AchievementCard
import com.example.scrolljourney.domain.ui.TrackingUiState
import com.example.scrolljourney.domain.ui.StatsUiState
import com.example.scrolljourney.domain.ui.StatsPeriod
import com.example.scrolljourney.domain.ui.CalibrationUiState
import com.example.scrolljourney.domain.ui.PrivacyUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Main ViewModel for the ScrollJourney app.
 * Manages state for all screens and coordinates repositories.
 */
class ScrollJourneyViewModel(
    private val statsRepository: ScrollStatsRepository,
    private val trackingController: TrackingController,
    private val gamificationRepository: GamificationRepository,
    private val calibrationRepository: CalibrationRepository
) : ViewModel() {

    // Dashboard State
    private val _dashboardState = MutableStateFlow(DashboardUiState())
    val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    // Tracking State
    private val _trackingState = MutableStateFlow(TrackingUiState())
    val trackingState: StateFlow<TrackingUiState> = _trackingState.asStateFlow()

    // Stats State
    private val _statsState = MutableStateFlow(StatsUiState())
    val statsState: StateFlow<StatsUiState> = _statsState.asStateFlow()

    // Achievements State
    private val _achievementsState = MutableStateFlow(AchievementsUiState())
    val achievementsState: StateFlow<AchievementsUiState> = _achievementsState.asStateFlow()

    // Calibration State
    private val _calibrationState = MutableStateFlow(CalibrationUiState())
    val calibrationState: StateFlow<CalibrationUiState> = _calibrationState.asStateFlow()

    // Privacy State
    private val _privacyState = MutableStateFlow(PrivacyUiState())
    val privacyState: StateFlow<PrivacyUiState> = _privacyState.asStateFlow()

    init {
        setupDashboardObservations()
        setupTrackingObservations()
        setupStatsObservations()
        setupAchievementsObservations()
    }

    private fun setupDashboardObservations() {
        // Combine today's stats with gamification data
        combine(
            statsRepository.observeToday(),
            gamificationRepository.observeCurrentXpAndLevel(),
            gamificationRepository.observeCurrentStreak(),
            gamificationRepository.observeLongestStreak(),
            trackingController.isTrackingEnabled()
        ) { todayStats, xpLevel, currentStreak, longestStreak, isTracking ->
            val (currentXp, currentLevel) = xpLevel
            val nextLevelXp = 1000L + (currentLevel * 500L) // Simple formula for demo
            val totalXpForLevel = 1000L + ((currentLevel - 1) * 500L)
            val xpInCurrentLevel = currentXp - totalXpForLevel
            val xpProgressPercent = if (nextLevelXp > 0) {
                (xpInCurrentLevel.toFloat() / nextLevelXp).coerceIn(0f, 1f)
            } else {
                0f
            }

            DashboardUiState(
                isLoading = false,
                totalDistanceMeters = todayStats.totalDistanceMeters,
                totalScrolls = todayStats.totalScrolls,
                currentXp = currentXp,
                currentLevel = currentLevel,
                xpToNextLevel = (nextLevelXp - xpInCurrentLevel).toLong(),
                xpProgressPercent = xpProgressPercent,
                currentStreak = currentStreak,
                streakLongestDays = longestStreak,
                topApps = todayStats.topApps,
                isTrackingActive = isTracking
            )
        }.onEach { newState ->
            Log.d(
                DEBUG_TAG,
                "Dashboard state updated: totalScrolls=${newState.totalScrolls} " +
                    "totalDistanceMeters=${newState.totalDistanceMeters} isTrackingActive=${newState.isTrackingActive}",
            )
            _dashboardState.value = newState
        }.launchIn(viewModelScope)
    }

    private fun setupTrackingObservations() {
        combine(
            trackingController.isTrackingEnabled(),
            trackingController.isServiceConnected(),
            trackingController.isAccessibilityPermissionGranted(),
            trackingController.isOverlayPermissionGranted()
        ) { isEnabled, isConnected, isPermissionGranted, isOverlayGranted ->
            TrackingUiState(
                isTrackingEnabled = isEnabled,
                serviceConnected = isConnected,
                isAccessibilityPermissionGranted = isPermissionGranted,
                isOverlayPermissionGranted = isOverlayGranted,
                isLoading = false
            )
        }.onEach { newState ->
            _trackingState.value = newState
        }.launchIn(viewModelScope)
    }

    private fun setupStatsObservations() {
        combine(
            statsRepository.observeToday(),
            statsRepository.observeWeek(),
            statsRepository.observeMonth()
        ) { today, week, month ->
            StatsUiState(
                selectedPeriod = StatsPeriod.TODAY,
                todayStats = today,
                weekStats = week,
                monthStats = month,
                appBreakdown = today.topApps,
                isLoading = false
            )
        }.onEach { newState ->
            _statsState.value = newState
        }.launchIn(viewModelScope)
    }

    private fun setupAchievementsObservations() {
        gamificationRepository.observeAchievements()
            .onEach { achievements ->
                val unlockedCount = achievements.count { it.isUnlocked }
                val unlockedPercent = if (achievements.isNotEmpty()) {
                    (unlockedCount.toFloat() / achievements.size) * 100f
                } else {
                    0f
                }

                val cards = achievements.map { achievement ->
                    AchievementCard(
                        id = achievement.id,
                        title = achievement.title,
                        description = achievement.description,
                        icon = achievement.icon,
                        isUnlocked = achievement.isUnlocked,
                        unlockedAtEpochMs = achievement.unlockedAtEpochMs,
                        xpReward = achievement.xpReward,
                        progress = achievement.progress,
                        progressMax = achievement.progressMax
                    )
                }

                _achievementsState.value = AchievementsUiState(
                    achievements = cards,
                    totalAchievementsUnlocked = unlockedCount,
                    unlockedPercentage = unlockedPercent,
                    isLoading = false
                )
            }.launchIn(viewModelScope)
    }

    fun setTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            trackingController.setTrackingEnabled(enabled)
        }
    }

    fun openAccessibilitySettings() {
        trackingController.openAccessibilitySettings()
    }

    fun openOverlaySettings() {
        trackingController.openOverlaySettings()
    }

    fun selectStatsPeriod(period: StatsPeriod) {
        _statsState.value = _statsState.value.copy(selectedPeriod = period)
    }

    private var calibrationProgressJob: Job? = null

    fun startCalibration() {
        val tracking = _trackingState.value
        if (!tracking.isTrackingEnabled || !tracking.serviceConnected) {
            _calibrationState.value = CalibrationUiState(
                error = "Enable tracking and grant accessibility permission before calibrating.",
            )
            return
        }
        calibrationProgressJob?.cancel()
        viewModelScope.launch {
            val id = calibrationRepository.startCalibration()
            calibrationProgressJob = calibrationRepository.observeCalibrationProgress(id)
                .onEach { progress ->
                    _calibrationState.value = CalibrationUiState(
                        isRunning = !progress.isCompleted,
                        isCompleted = progress.isCompleted,
                        sampleCount = progress.sampleCount,
                        targetSampleCount = progress.targetSampleCount,
                        medianDistanceMeters = progress.medianDistanceMeters,
                        meanDistanceMeters = progress.meanDistanceMeters,
                        progress = if (progress.targetSampleCount > 0) {
                            (progress.sampleCount.toFloat() / progress.targetSampleCount).coerceIn(0f, 1f)
                        } else {
                            0f
                        },
                        error = progress.error,
                    )
                }.launchIn(viewModelScope)
        }
    }

    fun cancelCalibration() {
        viewModelScope.launch {
            calibrationRepository.cancelCalibration()
        }
        calibrationProgressJob?.cancel()
        _calibrationState.value = CalibrationUiState()
    }

    companion object {
        /** TEMPORARY diagnostic tag for pipeline tracing; safe to remove once tracking is verified. */
        private const val DEBUG_TAG = "ScrollJourneyDebug"
    }
}
