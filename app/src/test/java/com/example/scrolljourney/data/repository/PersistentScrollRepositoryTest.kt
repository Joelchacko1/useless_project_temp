package com.example.scrolljourney.data.repository

import com.example.scrolljourney.data.persistence.ScrollDataStore
import com.example.scrolljourney.domain.data.EstimationMethod
import com.example.scrolljourney.domain.data.ProcessedScroll
import com.example.scrolljourney.domain.data.ScrollDirection
import com.example.scrolljourney.gamification.Achievement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.time.Instant

class PersistentScrollRepositoryTest {

    private val tempFiles = mutableListOf<File>()

    @After
    fun cleanUp() {
        tempFiles.forEach { it.delete() }
    }

    private fun newTempFile(): File =
        File.createTempFile("scroll_journey_test", ".json").also { tempFiles.add(it) }

    private fun newRepo(file: File = newTempFile()): PersistentScrollRepository =
        PersistentScrollRepository(ScrollDataStore(file), CoroutineScope(SupervisorJob() + Dispatchers.Unconfined))

    @Test
    fun testRecordScrollUpdatesStats() = runBlocking {
        val repo = newRepo()

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
        val repo = newRepo()
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
        val repo = newRepo()
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
        val repo = newRepo()
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

    @Test
    fun testDataSurvivesReload() = runBlocking {
        val file = newTempFile()
        val repo = newRepo(file)

        repo.recordScroll(
            ProcessedScroll(
                id = "1",
                timestampEpochMs = Instant.now().toEpochMilli(),
                packageName = "com.android.chrome",
                direction = ScrollDirection.UP,
                distanceMeters = 200.0,
                confidence = 1.0f,
                estimationMethod = EstimationMethod.ACTUAL_DELTA,
            ),
        )
        repo.flush()

        // Simulate the app being closed and reopened: a brand new repository instance backed
        // by the same file, with restoreFromDisk() standing in for app startup.
        val reopened = newRepo(file)
        reopened.restoreFromDisk()

        val todayStats = reopened.observeToday().first()
        assertEquals(200.0, todayStats.totalDistanceMeters, 0.001)
        assertEquals(1L, todayStats.totalScrolls)
        assertEquals(20L, reopened.gamificationState.value.totalXp) // 200 * 0.1 = 20 XP
    }

    @Test
    fun testCorruptOrMissingFileLoadsEmpty() = runBlocking {
        val missingFile = File.createTempFile("scroll_journey_test_missing", ".json")
        tempFiles.add(missingFile)
        missingFile.delete() // ensure it doesn't exist

        val persisted = ScrollDataStore(missingFile).load()
        assertEquals(0, persisted.events.size)
        assertEquals(0L, persisted.gamificationState.totalXp)
    }

    @Test
    fun testNewlyUnlockedAchievementEmitsOnceOnThresholdCrossing() = runBlocking {
        val repo = newRepo()
        val collected = mutableListOf<Achievement>()
        val job = launch(Dispatchers.Unconfined) {
            repo.newlyUnlockedAchievements.collect { collected.add(it) }
        }

        // Crosses the ACH_0_2KM (200m) threshold, but not the next one at 500m.
        repo.recordScroll(
            ProcessedScroll(
                id = "1",
                timestampEpochMs = Instant.now().toEpochMilli(),
                packageName = "com.android.chrome",
                direction = ScrollDirection.UP,
                distanceMeters = 250.0,
                confidence = 1.0f,
                estimationMethod = EstimationMethod.ACTUAL_DELTA,
            ),
        )
        assertEquals(1, collected.size)
        assertEquals("ACH_0_2KM", collected[0].id)

        // A further scroll that doesn't cross another threshold must not re-emit ACH_0_2KM.
        repo.recordScroll(
            ProcessedScroll(
                id = "2",
                timestampEpochMs = Instant.now().toEpochMilli(),
                packageName = "com.android.chrome",
                direction = ScrollDirection.UP,
                distanceMeters = 50.0,
                confidence = 1.0f,
                estimationMethod = EstimationMethod.ACTUAL_DELTA,
            ),
        )
        assertEquals(1, collected.size)

        job.cancel()
    }

    @Test
    fun testSingleScrollCrossingMultipleThresholdsEmitsEachOnce() = runBlocking {
        val repo = newRepo()
        val collected = mutableListOf<Achievement>()
        val job = launch(Dispatchers.Unconfined) {
            repo.newlyUnlockedAchievements.collect { collected.add(it) }
        }

        // One huge scroll crosses every threshold up to ACH_25KM, but not ACH_50KM.
        repo.recordScroll(
            ProcessedScroll(
                id = "1",
                timestampEpochMs = Instant.now().toEpochMilli(),
                packageName = "com.android.chrome",
                direction = ScrollDirection.UP,
                distanceMeters = 30000.0,
                confidence = 1.0f,
                estimationMethod = EstimationMethod.ACTUAL_DELTA,
            ),
        )

        assertEquals(
            setOf("ACH_0_2KM", "ACH_0_5KM", "ACH_1KM", "ACH_2KM", "ACH_5KM", "ACH_10KM", "ACH_25KM"),
            collected.map { it.id }.toSet(),
        )

        job.cancel()
    }
}
