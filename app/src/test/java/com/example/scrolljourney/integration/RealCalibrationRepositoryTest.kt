package com.example.scrolljourney.integration

import com.example.scrolljourney.domain.distance.CalibrationProfile
import com.example.scrolljourney.domain.distance.CalibrationProfileStore
import com.example.scrolljourney.domain.distance.EstimationMethod
import com.example.scrolljourney.domain.tracking.ProcessedScroll
import com.example.scrolljourney.domain.tracking.ScrollDirection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class RealCalibrationRepositoryTest {

    private fun scroll(
        estimationMethod: EstimationMethod,
        distanceMeters: Double,
        id: String,
        timestampEpochMs: Long,
    ): ProcessedScroll =
        ProcessedScroll(
            id = id,
            timestampEpochMs = timestampEpochMs,
            packageName = "com.android.chrome",
            direction = ScrollDirection.DOWN,
            distanceMeters = distanceMeters,
            confidence = 1.0f,
            estimationMethod = estimationMethod,
        )

    private class FakeCalibrationProfileStore : CalibrationProfileStore {
        var savedProfile: CalibrationProfile? = null
            private set

        override suspend fun activeProfile(): CalibrationProfile? = savedProfile

        override suspend fun saveProfile(profile: CalibrationProfile) {
            savedProfile = profile
        }
    }

    @Test
    fun testOnlyActualDeltaSamplesCountTowardTarget() = runBlocking {
        val processedScrolls = MutableSharedFlow<ProcessedScroll>(extraBufferCapacity = 64)
        val store = FakeCalibrationProfileStore()
        val repo = RealCalibrationRepository(
            processedScrolls = processedScrolls,
            profileStore = store,
            externalScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        )

        val id = repo.startCalibration()
        val progressUpdates = mutableListOf<Int>()
        val job = launch(Dispatchers.Unconfined) {
            repo.observeCalibrationProgress(id).collect { progressUpdates.add(it.sampleCount) }
        }

        val baseTime = Instant.now().toEpochMilli()
        // Interleave non-ACTUAL_DELTA scrolls, which must not count toward the target.
        processedScrolls.tryEmit(scroll(EstimationMethod.FALLBACK_ESTIMATE, 0.12, "noise-1", baseTime))
        repeat(19) { i ->
            processedScrolls.tryEmit(scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "actual-$i", baseTime + i * 600L))
        }
        processedScrolls.tryEmit(scroll(EstimationMethod.CALIBRATED_ESTIMATE, 0.2, "noise-2", baseTime + 19 * 600L))
        assertFalse(progressUpdates.contains(20)) // not complete yet — only 19 ACTUAL_DELTA samples so far

        processedScrolls.tryEmit(scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "actual-19", baseTime + 19 * 600L))

        assertEquals(20, progressUpdates.last())
        assertEquals(20, store.savedProfile?.sampleCount)

        job.cancel()
    }

    @Test
    fun testCompletedRunSavesProfileMatchingCalibrationEngine() = runBlocking {
        val processedScrolls = MutableSharedFlow<ProcessedScroll>(extraBufferCapacity = 64)
        val store = FakeCalibrationProfileStore()
        val repo = RealCalibrationRepository(
            processedScrolls = processedScrolls,
            profileStore = store,
            externalScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        )

        val id = repo.startCalibration()
        val progressUpdates = mutableListOf<Boolean>()
        val job = launch(Dispatchers.Unconfined) {
            repo.observeCalibrationProgress(id).collect { progressUpdates.add(it.isCompleted) }
        }

        val baseTime = Instant.now().toEpochMilli()
        repeat(20) { i ->
            processedScrolls.tryEmit(scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "actual-$i", baseTime + i * 600L))
        }

        assertTrue(progressUpdates.last())
        assertEquals(0.2, store.savedProfile?.medianDistanceMeters ?: -1.0, 0.0001)
        assertEquals(0.2, store.savedProfile?.meanDistanceMeters ?: -1.0, 0.0001)

        job.cancel()
    }

    @Test
    fun testCancelStopsProgressWithoutSaving() = runBlocking {
        val processedScrolls = MutableSharedFlow<ProcessedScroll>(extraBufferCapacity = 64)
        val store = FakeCalibrationProfileStore()
        val repo = RealCalibrationRepository(
            processedScrolls = processedScrolls,
            profileStore = store,
            externalScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        )

        repo.startCalibration()
        val baseTime = Instant.now().toEpochMilli()
        repeat(5) { i ->
            processedScrolls.tryEmit(scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "actual-$i", baseTime + i * 600L))
        }

        repo.cancelCalibration()

        // Samples that arrive after cancellation must not resurrect the old run.
        repeat(20) { i ->
            processedScrolls.tryEmit(
                scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "post-cancel-$i", baseTime + (5 + i) * 600L),
            )
        }

        assertNull(store.savedProfile)
    }

    @Test
    fun testSamplesFromTheSameGestureBurstCountOnce() = runBlocking {
        val processedScrolls = MutableSharedFlow<ProcessedScroll>(extraBufferCapacity = 64)
        val store = FakeCalibrationProfileStore()
        val repo = RealCalibrationRepository(
            processedScrolls = processedScrolls,
            profileStore = store,
            externalScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        )

        val id = repo.startCalibration()
        val progressUpdates = mutableListOf<Int>()
        val job = launch(Dispatchers.Unconfined) {
            repo.observeCalibrationProgress(id).collect { progressUpdates.add(it.sampleCount) }
        }

        // One continuous fling: 20 callbacks only 10ms apart, spanning 190ms total — well within
        // the gesture gap, unlike the inter-callback gap alone (the total burst span matters,
        // not just consecutive-callback spacing, since the gate compares against the last
        // *accepted* sample).
        val burstStart = Instant.now().toEpochMilli()
        repeat(20) { i ->
            processedScrolls.tryEmit(scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "burst-$i", burstStart + i * 10L))
        }
        assertEquals(1, progressUpdates.last())
        assertNull(store.savedProfile)

        // Properly spaced gestures afterward should still complete the run normally.
        val secondPhaseStart = burstStart + 20 * 10L + 600L
        repeat(19) { i ->
            processedScrolls.tryEmit(
                scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "spaced-$i", secondPhaseStart + i * 600L),
            )
        }
        assertEquals(20, progressUpdates.last())
        assertEquals(20, store.savedProfile?.sampleCount)

        job.cancel()
    }
}
