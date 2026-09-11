package com.example.scrolljourney.integration

import com.example.scrolljourney.domain.repository.CalibrationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CalibrationRepositoryStub : CalibrationRepository {

    override suspend fun startCalibration(): String = "stub-calibration"

    override suspend fun cancelCalibration() {}

    override fun observeCalibrationProgress(
        calibrationId: String,
    ): Flow<CalibrationRepository.CalibrationProgress> = flowOf(
        CalibrationRepository.CalibrationProgress(
            calibrationId = calibrationId,
            sampleCount = 0,
            targetSampleCount = 20,
            medianDistanceMeters = 0.0,
            meanDistanceMeters = 0.0,
            isCompleted = false,
        ),
    )
}
