package com.example.scrolljourney.domain.distance

import kotlinx.serialization.Serializable

/**
 * A persisted calibration summary. The median is the robust value used for estimates; the mean
 * is retained for diagnostics and future calibration UX.
 */
@Serializable
data class CalibrationProfile(
    val id: String,
    val createdAtEpochMs: Long,
    val sampleCount: Int,
    val medianDistanceMeters: Double,
    val meanDistanceMeters: Double,
    val lastUpdatedEpochMs: Long,
)

@Serializable
enum class EstimationMethod {
    ACTUAL_DELTA,
    CALIBRATED_ESTIMATE,
    FALLBACK_ESTIMATE,
}

data class DistanceEstimate(
    val distanceMeters: Double,
    val confidence: Float,
    val estimationMethod: EstimationMethod,
)

/** Implemented by Agent 3's data layer and supplied to tracking by app-level integration. */
interface CalibrationProfileStore {
    suspend fun activeProfile(): CalibrationProfile?

    suspend fun saveProfile(profile: CalibrationProfile)
}

/** Read-only variant useful when the tracking service must not write calibration data. */
interface CalibrationProfileProvider {
    suspend fun activeProfile(): CalibrationProfile?
}

/** Default provider used until Agent 4 wires Agent 3's persistence implementation. */
object EmptyCalibrationProfileProvider : CalibrationProfileProvider {
    override suspend fun activeProfile(): CalibrationProfile? = null
}
