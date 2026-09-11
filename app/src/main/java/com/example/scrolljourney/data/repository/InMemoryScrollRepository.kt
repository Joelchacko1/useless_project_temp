package com.example.scrolljourney.data.repository

import com.example.scrolljourney.domain.data.*
import com.example.scrolljourney.gamification.GamificationEngine
import com.example.scrolljourney.gamification.GamificationState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * An in-memory implementation of the ScrollEventSink and ScrollStatsRepository.
 * This is provided to ensure the project remains buildable and testable 
 * while the Room dependencies are being integrated by Agent 4.
 */
class InMemoryScrollRepository : ScrollEventSink, ScrollStatsRepository {

    private val events = mutableListOf<ProcessedScroll>()
    private val statsFlow = MutableStateFlow(events.toList())
    
    // In-memory gamification state
    var gamificationState = GamificationState(
        totalXp = 0L,
        currentLevel = 1,
        currentStreakDays = 0,
        lastActiveDateKey = null,
        unlockedAchievements = emptyList()
    )

    override suspend fun recordScroll(event: ProcessedScroll) {
        events.add(event)
        statsFlow.value = events.toList()
        updateGamification(event)
    }

    private fun updateGamification(event: ProcessedScroll) {
        val dateKey = DateUtils.getDateKey(event.timestampEpochMs)
        
        val totalDistance = events.sumOf { it.distanceMeters }
        val newXp = GamificationEngine.calculateXp(totalDistance)
        val newLevel = GamificationEngine.calculateLevel(newXp)
        val newAchievements = GamificationEngine.evaluateAchievements(totalDistance, gamificationState.unlockedAchievements)
        val newStreak = GamificationEngine.updateStreak(
            gamificationState.currentStreakDays, 
            gamificationState.lastActiveDateKey, 
            dateKey
        )

        gamificationState = gamificationState.copy(
            totalXp = newXp,
            currentLevel = newLevel,
            unlockedAchievements = newAchievements.filter { it.isUnlocked }.map { it.id },
            currentStreakDays = newStreak,
            lastActiveDateKey = dateKey
        )
    }

    override fun observeToday(): Flow<AggregatedStats> {
        return observeForDateRange(0) // Today
    }

    override fun observeWeek(): Flow<AggregatedStats> {
        return observeForDateRange(7) // Last 7 days
    }

    override fun observeMonth(): Flow<AggregatedStats> {
        return observeForDateRange(30) // Last 30 days
    }

    override fun observeAppBreakdown(dateKey: String): Flow<List<AppDistanceStat>> {
        return statsFlow.map { allEvents ->
            allEvents
                .filter { DateUtils.getDateKey(it.timestampEpochMs) == dateKey }
                .groupBy { it.packageName }
                .map { (packageName, pkgEvents) ->
                    AppDistanceStat(
                        packageName = packageName,
                        distanceMeters = pkgEvents.sumOf { it.distanceMeters }
                    )
                }
                .sortedByDescending { it.distanceMeters }
        }
    }

    private fun observeForDateRange(daysBack: Long): Flow<AggregatedStats> {
        return statsFlow.map { allEvents ->
            val today = LocalDate.now()
            
            val filteredEvents = if (daysBack == 0L) {
                // Today
                val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                allEvents.filter { DateUtils.getDateKey(it.timestampEpochMs) == todayStr }
            } else {
                // Last N days
                allEvents.filter {
                    val eventDateStr = DateUtils.getDateKey(it.timestampEpochMs)
                    val eventDate = LocalDate.parse(eventDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                    val daysBetween = ChronoUnit.DAYS.between(eventDate, today)
                    daysBetween in 0..daysBack
                }
            }

            val totalDistance = filteredEvents.sumOf { it.distanceMeters }
            val totalCount = filteredEvents.size.toLong()
            
            val topApps = filteredEvents
                .groupBy { it.packageName }
                .map { (pkg, evts) -> AppDistanceStat(pkg, evts.sumOf { it.distanceMeters }) }
                .sortedByDescending { it.distanceMeters }

            // Approximation of active tracking time based on count * average scroll duration (e.g. 50ms)
            // A real implementation might use start/stop events
            val activeMs = totalCount * 50L 

            AggregatedStats(
                dateKey = if (daysBack == 0L) today.format(DateTimeFormatter.ISO_LOCAL_DATE) else "Last ${daysBack} Days",
                totalScrolls = totalCount,
                totalDistanceMeters = totalDistance,
                activeTrackingMs = activeMs,
                topApps = topApps
            )
        }
    }
}
