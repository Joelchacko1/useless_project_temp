package com.example.scrolljourney.integration

import com.example.scrolljourney.domain.distance.CalibrationEngine
import com.example.scrolljourney.domain.distance.CalibrationProfileStore
import com.example.scrolljourney.domain.distance.EstimationMethod
import com.example.scrolljourney.domain.repository.CalibrationRepository
import com.example.scrolljourney.domain.tracking.ProcessedScroll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Runs a real calibration session: harvests the user's own already-reliable [EstimationMethod.ACTUAL_DELTA]
 * scroll measurements (no manual "scroll a known distance" step needed — see [CalibrationEngine]),
 * reduces them to a robust profile, and saves it for [com.example.scrolljourney.domain.distance.HybridDistanceCalculator]
 * to use as a personalized substitute for scrolls it can't measure directly.
 */
class RealCalibrationRepository(
    private val processedScrolls: SharedFlow<ProcessedScroll>,
    private val profileStore: CalibrationProfileStore,
    private val externalScope: CoroutineScope,
    private val calibrationEngine: CalibrationEngine = CalibrationEngine(),
) : CalibrationRepository {

    private val _progress = MutableStateFlow(IDLE)
    private var runJob: Job? = null

    override suspend fun startCalibration(): String {
        runJob?.cancel()
        val id = UUID.randomUUID().toString()
        _progress.value = IDLE.copy(calibrationId = id)
        runJob = externalScope.launch {
            val samples = mutableListOf<Double>()
            processedScrolls
                .filter { it.estimationMethod == EstimationMethod.ACTUAL_DELTA }
                .take(TARGET_SAMPLE_COUNT)
                .collect { sample ->
                    samples += sample.distanceMeters
                    _progress.value = _progress.value.copy(sampleCount = samples.size)
                }

            val computation = calibrationEngine.createProfile(id, samples, System.currentTimeMillis())
            withContext(NonCancellable) {
                if (computation != null) {
                    profileStore.saveProfile(computation.profile)
                    _progress.value = _progress.value.copy(
                        isCompleted = true,
                        medianDistanceMeters = computation.profile.medianDistanceMeters,
                        meanDistanceMeters = computation.profile.meanDistanceMeters,
                    )
                } else {
                    _progress.value = _progress.value.copy(
                        isCompleted = true,
                        error = "Not enough reliable scroll samples were collected. Try again.",
                    )
                }
            }
        }
        return id
    }

    override suspend fun cancelCalibration() {
        runJob?.cancel()
        runJob = null
        _progress.value = IDLE
    }

    override fun observeCalibrationProgress(
        calibrationId: String,
    ): Flow<CalibrationRepository.CalibrationProgress> = _progress.asStateFlow()

    companion object {
        const val TARGET_SAMPLE_COUNT = 20

        private val IDLE = CalibrationRepository.CalibrationProgress(
            calibrationId = "",
            sampleCount = 0,
            targetSampleCount = TARGET_SAMPLE_COUNT,
            medianDistanceMeters = 0.0,
            meanDistanceMeters = 0.0,
            isCompleted = false,
        )
    }
}
