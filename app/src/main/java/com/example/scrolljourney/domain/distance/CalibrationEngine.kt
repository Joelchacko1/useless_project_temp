package com.example.scrolljourney.domain.distance

import kotlin.math.abs
import kotlin.math.max

/** Result of a calibration run, including enough diagnostics for a future calibration screen. */
data class CalibrationComputation(
    val profile: CalibrationProfile,
    val acceptedMeasurementsMeters: List<Double>,
    val rejectedMeasurementCount: Int,
) {
    /** The median is the robust calibration value consumed by [HybridDistanceCalculator]. */
    val robustDistanceMeters: Double
        get() = profile.medianDistanceMeters
}

/**
 * Stateless, deterministic calibration builder.
 *
 * Measurements are first range-checked, then filtered with a median absolute deviation (MAD)
 * fence. The minimum absolute fence prevents a zero-MAD group of near-identical samples from
 * incorrectly rejecting ordinary small device variation.
 */
class CalibrationEngine {
    fun createProfile(
        profileId: String,
        measurementsMeters: List<Double>,
        nowEpochMs: Long,
    ): CalibrationComputation? {
        require(profileId.isNotBlank()) { "profileId must not be blank" }
        require(nowEpochMs >= 0L) { "nowEpochMs must not be negative" }

        val rangeAccepted = measurementsMeters.filter(::isInCalibrationRange)
        if (rangeAccepted.isEmpty()) return null

        val initialMedian = median(rangeAccepted)
        val medianAbsoluteDeviation = median(rangeAccepted.map { abs(it - initialMedian) })
        val permittedDeviation = max(
            medianAbsoluteDeviation * MAD_MULTIPLIER,
            max(MIN_OUTLIER_DEVIATION_METERS, initialMedian * MAX_DEVIATION_FRACTION),
        )
        val robustAccepted = rangeAccepted.filter {
            abs(it - initialMedian) <= permittedDeviation
        }
        if (robustAccepted.isEmpty()) return null

        val sortedAccepted = robustAccepted.sorted()
        val profile = CalibrationProfile(
            id = profileId,
            createdAtEpochMs = nowEpochMs,
            sampleCount = sortedAccepted.size,
            medianDistanceMeters = median(sortedAccepted),
            meanDistanceMeters = sortedAccepted.average(),
            lastUpdatedEpochMs = nowEpochMs,
        )
        return CalibrationComputation(
            profile = profile,
            acceptedMeasurementsMeters = sortedAccepted,
            rejectedMeasurementCount = measurementsMeters.size - sortedAccepted.size,
        )
    }

    private fun isInCalibrationRange(value: Double): Boolean =
        value.isFinite() && value in MIN_CALIBRATION_DISTANCE_METERS..MAX_CALIBRATION_DISTANCE_METERS

    private fun median(values: List<Double>): Double {
        val sorted = values.sorted()
        val middle = sorted.size / 2
        return if (sorted.size % 2 == 0) {
            (sorted[middle - 1] + sorted[middle]) / 2.0
        } else {
            sorted[middle]
        }
    }

    companion object {
        /** Reject values below two millimetres; they are callback noise rather than a calibration. */
        const val MIN_CALIBRATION_DISTANCE_METERS = 0.002

        /** Reject multi-metre single callbacks as malformed or accidental calibration input. */
        const val MAX_CALIBRATION_DISTANCE_METERS = 5.0

        /** Standard robust MAD multiplier, relaxed by the named absolute/relative floors below. */
        const val MAD_MULTIPLIER = 3.5

        /** Allows normal low-distance sensor and conversion variation around a zero-MAD median. */
        const val MIN_OUTLIER_DEVIATION_METERS = 0.02

        /** Avoids treating a modest, proportionate calibration variation as an outlier. */
        const val MAX_DEVIATION_FRACTION = 0.35
    }
}
