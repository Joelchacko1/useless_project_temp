package com.example.scrolljourney.data.repository

import com.example.scrolljourney.domain.data.EstimationMethod
import com.example.scrolljourney.domain.data.ProcessedScroll
import com.example.scrolljourney.domain.data.ScrollDirection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class InMemoryScrollRepositoryTest {

    @Test
    fun testRecordScrollUpdatesStats() = runBlocking {
        val repo = InMemoryScrollRepository()
        
        val event1 = ProcessedScroll(
            id = "1",
            timestampEpochMs = Instant.now().toEpochMilli(),
            packageName = "com.android.chrome",
            direction = ScrollDirection.UP,
            distanceMeters = 200.0,
            confidence = 1.0f,
            estimationMethod = EstimationMethod.ACTUAL_DELTA
        )
        
        val event2 = ProcessedScroll(
            id = "2",
            timestampEpochMs = Instant.now().toEpochMilli(),
            packageName = "com.google.android.youtube",
            direction = ScrollDirection.DOWN,
            distanceMeters = 150.0,
            confidence = 0.8f,
            estimationMethod = EstimationMethod.CALIBRATED_ESTIMATE
        )

        repo.recordScroll(event1)
        repo.recordScroll(event2)

        val todayStats = repo.observeToday().first()
        assertEquals(350.0, todayStats.totalDistanceMeters, 0.001)
        assertEquals(2, todayStats.totalScrolls)
        
        val appBreakdown = todayStats.topApps
        assertEquals(2, appBreakdown.size)
        assertEquals("com.android.chrome", appBreakdown[0].packageName) // 200 > 150
        assertEquals(200.0, appBreakdown[0].distanceMeters, 0.001)
        
        // Gamification
        val gamification = repo.gamificationState.value
        assertEquals(35L, gamification.totalXp) // 350 * 0.1 = 35 XP
        assertEquals(1, gamification.currentLevel) // 35 XP = Level 1
        assertEquals(1, gamification.currentStreakDays) // First day of activity
    }

    @Test
    fun testBurstOfCallbacksCountsAsOneScroll() = runBlocking {
        val repo = InMemoryScrollRepository()
        val baseTime = Instant.now().toEpochMilli()

        repeat(20) { i ->
            repo.recordScroll(
                ProcessedScroll(
                    id = "burst-$i",
                    timestampEpochMs = baseTime + i * 75L, // 75ms apart, well within the gesture window
                    packageName = "com.android.chrome",
                    direction = ScrollDirection.DOWN,
                    distanceMeters = 1.0,
                    confidence = 1.0f,
                    estimationMethod = EstimationMethod.ACTUAL_DELTA,
                ),
            )
        }

        val todayStats = repo.observeToday().first()
        assertEquals(1L, todayStats.totalScrolls)
        assertEquals(1, todayStats.topApps.size)
        assertEquals(1L, todayStats.topApps[0].scrollCount)
    }

    @Test
    fun testGapLargerThanThresholdSplitsIntoTwoScrolls() = runBlocking {
        val repo = InMemoryScrollRepository()
        val baseTime = Instant.now().toEpochMilli()

        // First burst
        repeat(5) { i ->
            repo.recordScroll(
                ProcessedScroll(
                    id = "burst1-$i",
                    timestampEpochMs = baseTime + i * 75L,
                    packageName = "com.android.chrome",
                    direction = ScrollDirection.DOWN,
                    distanceMeters = 1.0,
                    confidence = 1.0f,
                    estimationMethod = EstimationMethod.ACTUAL_DELTA,
                ),
            )
        }

        // Second burst, well beyond the 500ms gesture gap
        val secondBurstStart = baseTime + 1500L
        repeat(5) { i ->
            repo.recordScroll(
                ProcessedScroll(
                    id = "burst2-$i",
                    timestampEpochMs = secondBurstStart + i * 75L,
                    packageName = "com.android.chrome",
                    direction = ScrollDirection.DOWN,
                    distanceMeters = 1.0,
                    confidence = 1.0f,
                    estimationMethod = EstimationMethod.ACTUAL_DELTA,
                ),
            )
        }

        val todayStats = repo.observeToday().first()
        assertEquals(2L, todayStats.totalScrolls)
        assertEquals(2L, todayStats.topApps[0].scrollCount)
    }

    @Test
    fun testActiveTrackingMsReflectsRawCallbackVolumeNotGestureCount() = runBlocking {
        val repo = InMemoryScrollRepository()
        val baseTime = Instant.now().toEpochMilli()
        val callbackCount = 10

        repeat(callbackCount) { i ->
            repo.recordScroll(
                ProcessedScroll(
                    id = "cb-$i",
                    timestampEpochMs = baseTime + i * 75L,
                    packageName = "com.android.chrome",
                    direction = ScrollDirection.DOWN,
                    distanceMeters = 1.0,
                    confidence = 1.0f,
                    estimationMethod = EstimationMethod.ACTUAL_DELTA,
                ),
            )
        }

        val todayStats = repo.observeToday().first()
        assertEquals(1L, todayStats.totalScrolls) // still one gesture
        assertEquals(callbackCount * 50L, todayStats.activeTrackingMs) // but active time scales with raw callbacks
    }
}
