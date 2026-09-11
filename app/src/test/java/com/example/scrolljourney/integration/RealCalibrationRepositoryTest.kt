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

    private fun scroll(estimationMethod: EstimationMethod, distanceMeters: Double, id: String): ProcessedScroll =
        ProcessedScroll(
            id = id,
            timestampEpochMs = Instant.now().toEpochMilli(),
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

        // Interleave non-ACTUAL_DELTA scrolls, which must not count toward the target.
        processedScrolls.tryEmit(scroll(EstimationMethod.FALLBACK_ESTIMATE, 0.12, "noise-1"))
        repeat(19) { i ->
            processedScrolls.tryEmit(scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "actual-$i"))
        }
        processedScrolls.tryEmit(scroll(EstimationMethod.CALIBRATED_ESTIMATE, 0.2, "noise-2"))
        assertFalse(progressUpdates.contains(20)) // not complete yet — only 19 ACTUAL_DELTA samples so far

        processedScrolls.tryEmit(scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "actual-19"))

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

        repeat(20) { i ->
            processedScrolls.tryEmit(scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "actual-$i"))
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
        repeat(5) { i ->
            processedScrolls.tryEmit(scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "actual-$i"))
        }

        repo.cancelCalibration()

        // Samples that arrive after cancellation must not resurrect the old run.
        repeat(20) { i ->
            processedScrolls.tryEmit(scroll(EstimationMethod.ACTUAL_DELTA, 0.2, "post-cancel-$i"))
        }

        assertNull(store.savedProfile)
    }
}
