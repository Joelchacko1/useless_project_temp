package com.example.scrolljourney.data.repository

import android.util.Log
import com.example.scrolljourney.data.persistence.PersistedScrollData
import com.example.scrolljourney.data.persistence.ScrollDataStore
import com.example.scrolljourney.domain.data.*
import com.example.scrolljourney.gamification.Achievement
import com.example.scrolljourney.gamification.GamificationEngine
import com.example.scrolljourney.gamification.GamificationState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** TEMPORARY diagnostic tag for pipeline tracing; safe to remove once tracking is verified. */
private const val DEBUG_TAG = "ScrollJourneyDebug"

/**
 * The ScrollEventSink and ScrollStatsRepository implementation, backed by a JSON file via
 * [ScrollDataStore] so tracked history survives the app being closed. Call [restoreFromDisk]
 * once at startup before relying on [observeToday]/[observeWeek]/[observeMonth], and [flush]
 * when the app is about to leave the foreground.
 */
@OptIn(FlowPreview::class)
class PersistentScrollRepository(
    private val dataStore: ScrollDataStore,
    private val externalScope: CoroutineScope,
) : ScrollEventSink, ScrollStatsRepository {

    private val events = mutableListOf<ProcessedScroll>()
    private val eventsMutex = Mutex()
    private val statsFlow = MutableStateFlow(events.toList())

    private val _gamificationState = MutableStateFlow(
        GamificationState(
            totalXp = 0L,
            currentLevel = 1,
            currentStreakDays = 0,
            lastActiveDateKey = null,
            unlockedAchievements = emptyList(),
        ),
    )
    val gamificationState: StateFlow<GamificationState> = _gamificationState.asStateFlow()

    private val _newlyUnlockedAchievements = MutableSharedFlow<Achievement>(extraBufferCapacity = 4)

    /** Emits once per achievement the moment it transitions from locked to unlocked. No replay — a one-shot celebratory event stream, not steady state. */
    val newlyUnlockedAchievements: SharedFlow<Achievement> = _newlyUnlockedAchievements.asSharedFlow()

    private val _processedScrolls = MutableSharedFlow<ProcessedScroll>(extraBufferCapacity = 16)

    /** Emits every processed scroll as it's recorded — e.g. consumed by calibration to harvest ACTUAL_DELTA samples. */
    val processedScrolls: SharedFlow<ProcessedScroll> = _processedScrolls.asSharedFlow()

    private val saveRequests = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        externalScope.launch {
            saveRequests.debounce(SAVE_DEBOUNCE_MS).collect {
                dataStore.save(currentSnapshot())
            }
        }
    }

    /** Loads previously persisted history from disk. Call once at startup. */
    suspend fun restoreFromDisk() {
        val persisted = dataStore.load()
        eventsMutex.withLock {
            events.clear()
            events.addAll(persisted.events)
            statsFlow.value = events.toList()
        }
        _gamificationState.value = persisted.gamificationState
    }

    /** Saves immediately, bypassing the debounce — use when the app is leaving the foreground. */
    suspend fun flush() {
        dataStore.save(currentSnapshot())
    }

    private fun currentSnapshot(): PersistedScrollData =
        PersistedScrollData(events = statsFlow.value, gamificationState = _gamificationState.value)

    override suspend fun recordScroll(event: ProcessedScroll) {
        eventsMutex.withLock {
            events.add(event)
            statsFlow.value = events.toList()
        }
        Log.d(
            DEBUG_TAG,
            "Repository recorded scroll #${events.size} package=${event.packageName} " +
                "meters=${event.distanceMeters} timestampEpochMs=${event.timestampEpochMs} " +
                "dateKey=${DateUtils.getDateKey(event.timestampEpochMs)}",
        )
        updateGamification(event)
        saveRequests.tryEmit(Unit)
        _processedScrolls.tryEmit(event)
    }

    private fun updateGamification(event: ProcessedScroll) {
        val dateKey = DateUtils.getDateKey(event.timestampEpochMs)
        val current = _gamificationState.value

        val totalDistance = events.sumOf { it.distanceMeters }
        val newXp = GamificationEngine.calculateXp(totalDistance)
        val newLevel = GamificationEngine.calculateLevel(newXp)
        val newAchievements = GamificationEngine.evaluateAchievements(totalDistance, current.unlockedAchievements)
        val newStreak = GamificationEngine.updateStreak(
            current.currentStreakDays,
            current.lastActiveDateKey,
            dateKey,
        )

        val previouslyUnlockedIds = current.unlockedAchievements.toSet()
        val newlyUnlocked = newAchievements.filter { it.isUnlocked && it.id !in previouslyUnlockedIds }

        _gamificationState.value = current.copy(
            totalXp = newXp,
            currentLevel = newLevel,
            unlockedAchievements = newAchievements.filter { it.isUnlocked }.map { it.id },
            currentStreakDays = newStreak,
            lastActiveDateKey = dateKey,
        )

        newlyUnlocked.forEach { _newlyUnlockedAchievements.tryEmit(it) }
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
                        distanceMeters = pkgEvents.sumOf { it.distanceMeters },
                        scrollCount = countScrollGestures(pkgEvents),
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
            // Raw callback count (NOT gesture count) — intentionally used for activeMs below,
            // since a burst of many callbacks still represents more actual scrolling time.
            val totalCount = filteredEvents.size.toLong()
            val totalGestureCount = countScrollGestures(filteredEvents)

            val topApps = filteredEvents
                .groupBy { it.packageName }
                .map { (pkg, evts) ->
                    AppDistanceStat(
                        packageName = pkg,
                        distanceMeters = evts.sumOf { it.distanceMeters },
                        scrollCount = countScrollGestures(evts),
                    )
                }
                .sortedByDescending { it.distanceMeters }

            // Approximation of active tracking time based on count * average scroll duration (e.g. 50ms)
            // A real implementation might use start/stop events
            val activeMs = totalCount * 50L

            AggregatedStats(
                dateKey = if (daysBack == 0L) today.format(DateTimeFormatter.ISO_LOCAL_DATE) else "Last ${daysBack} Days",
                totalScrolls = totalGestureCount,
                totalDistanceMeters = totalDistance,
                activeTrackingMs = activeMs,
                topApps = topApps
            )
        }
    }

    /**
     * Counts distinct scroll "gestures" in [events]: a burst of consecutive callbacks from the
     * same app with no gap longer than [GESTURE_SESSION_GAP_MS] counts as one gesture. Events
     * are grouped by packageName before counting, since a scroll in app A and a scroll in app B
     * happening close together in time are always two separate physical gestures.
     *
     * This only changes how scrolls are counted for display; distance accumulation is untouched
     * and keeps summing distanceMeters from every raw callback.
     */
    private fun countScrollGestures(events: List<ProcessedScroll>): Long {
        return events
            .groupBy { it.packageName }
            .values
            .sumOf { pkgEvents ->
                val sorted = pkgEvents.sortedBy { it.timestampEpochMs }
                var gestures = 1L
                for (i in 1 until sorted.size) {
                    if (sorted[i].timestampEpochMs - sorted[i - 1].timestampEpochMs > GESTURE_SESSION_GAP_MS) {
                        gestures++
                    }
                }
                gestures
            }
    }

    companion object {
        /**
         * Max gap (ms) between consecutive callbacks from the same app to be considered part of
         * one continuous scroll gesture. Android fires TYPE_VIEW_SCROLLED once per frame during
         * a single swipe, so without this a single gesture is counted as dozens of "scrolls".
         * Independent from ScrollEventProcessor.DUPLICATE_CALLBACK_WINDOW_MS (250ms), which
         * suppresses byte-identical duplicate callback metadata at ingestion — a different
         * problem at a different layer. This only affects display counting, never distance
         * accumulation.
         */
        internal const val GESTURE_SESSION_GAP_MS = 500L

        /**
         * How long to wait after the last recorded scroll before writing to disk. A continuous
         * scroll burst can fire dozens of callbacks per second; without this, every one of them
         * would trigger its own disk write.
         */
        private const val SAVE_DEBOUNCE_MS = 1000L
    }
}
