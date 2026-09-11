package com.example.scrolljourney.domain.distance

import android.util.DisplayMetrics
import com.example.scrolljourney.domain.tracking.RawScrollEvent
import com.example.scrolljourney.domain.tracking.ScrollDirection
import kotlin.math.abs
import kotlin.math.max

/**
 * Converts numeric accessibility scroll data into an estimated physical distance.
 *
 * Android reports a displacement in pixels, rather than a physical measurement. For the
 * ACTUAL_DELTA path this calculator uses the current device's reported physical pixel density.
 * A calibrated median is used when an event has movement evidence but no reliable pixel delta.
 */
class HybridDistanceCalculator(
    private val deviceDistanceConfig: DeviceDistanceConfig,
) {
    fun estimate(
        event: RawScrollEvent,
        calibrationProfile: CalibrationProfile?,
    ): DistanceEstimate {
        val primaryDelta = primaryReliableDelta(event)
        if (primaryDelta != null) {
            val pixelsPerMeter = when (primaryDelta.axis) {
                ScrollAxis.HORIZONTAL -> deviceDistanceConfig.horizontalPixelsPerMeter
                ScrollAxis.VERTICAL -> deviceDistanceConfig.verticalPixelsPerMeter
            }
            return DistanceEstimate(
                distanceMeters = primaryDelta.magnitudePixels / pixelsPerMeter,
                confidence = ACTUAL_DELTA_CONFIDENCE,
                estimationMethod = EstimationMethod.ACTUAL_DELTA,
            )
        }

        val calibratedDistance = calibrationProfile
            ?.takeIf(::isUsableCalibration)
            ?.medianDistanceMeters
        if (calibratedDistance != null) {
            return DistanceEstimate(
                distanceMeters = calibratedDistance,
                confidence = calibrationConfidence(calibrationProfile.sampleCount),
                estimationMethod = EstimationMethod.CALIBRATED_ESTIMATE,
            )
        }

        return DistanceEstimate(
            distanceMeters = deviceDistanceConfig.fallbackScrollDistanceMeters,
            confidence = FALLBACK_ESTIMATE_CONFIDENCE,
            estimationMethod = EstimationMethod.FALLBACK_ESTIMATE,
        )
    }

    private fun primaryReliableDelta(event: RawScrollEvent): PrimaryDelta? {
        val horizontal = event.deltaX?.toLong()?.let(::safeAbsolute)
        val vertical = event.deltaY?.toLong()?.let(::safeAbsolute)
        val axis = when {
            vertical != null && horizontal != null && vertical >= horizontal -> ScrollAxis.VERTICAL
            vertical != null -> ScrollAxis.VERTICAL
            horizontal != null -> ScrollAxis.HORIZONTAL
            else -> return null
        }
        val magnitude = when (axis) {
            ScrollAxis.HORIZONTAL -> horizontal ?: return null
            ScrollAxis.VERTICAL -> vertical ?: return null
        }
        if (magnitude !in MIN_RELIABLE_DELTA_PIXELS..MAX_RELIABLE_DELTA_PIXELS) return null
        return PrimaryDelta(axis, magnitude.toDouble())
    }

    private fun isUsableCalibration(profile: CalibrationProfile): Boolean =
        profile.sampleCount >= MIN_CALIBRATION_SAMPLE_COUNT &&
            profile.medianDistanceMeters.isFinite() &&
            profile.medianDistanceMeters in MIN_CALIBRATED_DISTANCE_METERS..MAX_CALIBRATED_DISTANCE_METERS

    private fun calibrationConfidence(sampleCount: Int): Float {
        val sampleBonus = (sampleCount.coerceAtMost(CONFIDENCE_FULL_SAMPLE_COUNT).toFloat() /
            CONFIDENCE_FULL_SAMPLE_COUNT) * CALIBRATION_MAX_SAMPLE_BONUS
        return CALIBRATION_BASE_CONFIDENCE + sampleBonus
    }

    private fun safeAbsolute(value: Long): Long = if (value == Long.MIN_VALUE) Long.MAX_VALUE else abs(value)

    private data class PrimaryDelta(
        val axis: ScrollAxis,
        val magnitudePixels: Double,
    )

    private enum class ScrollAxis {
        HORIZONTAL,
        VERTICAL,
    }

    companion object {
        /** Ignore zero/one-pixel callback noise before treating a delta as physical displacement. */
        const val MIN_RELIABLE_DELTA_PIXELS = 2L

        /** Ignore implausibly large callback deltas, which usually indicate malformed metadata. */
        const val MAX_RELIABLE_DELTA_PIXELS = 20_000L

        const val MIN_CALIBRATION_SAMPLE_COUNT = 1
        const val MIN_CALIBRATED_DISTANCE_METERS = 0.002
        const val MAX_CALIBRATED_DISTANCE_METERS = 5.0

        const val ACTUAL_DELTA_CONFIDENCE = 0.90f
        const val CALIBRATION_BASE_CONFIDENCE = 0.55f
        const val CALIBRATION_MAX_SAMPLE_BONUS = 0.25f
        const val CONFIDENCE_FULL_SAMPLE_COUNT = 20
        const val FALLBACK_ESTIMATE_CONFIDENCE = 0.25f
    }
}

/**
 * Device-specific conversion inputs. Values are kept injectable so distance calculations are
 * deterministic in tests and can be refined by future device calibration work.
 */
data class DeviceDistanceConfig(
    val horizontalPixelsPerMeter: Double,
    val verticalPixelsPerMeter: Double,
    val fallbackScrollDistanceMeters: Double = DEFAULT_FALLBACK_SCROLL_DISTANCE_METERS,
) {
    init {
        require(horizontalPixelsPerMeter > 0.0 && horizontalPixelsPerMeter.isFinite())
        require(verticalPixelsPerMeter > 0.0 && verticalPixelsPerMeter.isFinite())
        require(fallbackScrollDistanceMeters > 0.0 && fallbackScrollDistanceMeters.isFinite())
    }

    companion object {
        private const val INCHES_PER_METER = 39.37007874015748
        private const val MIN_PLAUSIBLE_DPI = 40.0f
        private const val MAX_PLAUSIBLE_DPI = 1_000.0f
        private const val BASELINE_DENSITY_DPI = 160.0

        /**
         * Conservative pre-calibration estimate. It is deliberately low-confidence and replaced
         * by a user calibration as soon as one is available; it is not presented as exact.
         */
        const val DEFAULT_FALLBACK_SCROLL_DISTANCE_METERS = 0.12

        fun fromDisplayMetrics(displayMetrics: DisplayMetrics): DeviceDistanceConfig {
            val horizontalDpi = displayMetrics.xdpi.takeIf(::isPlausibleDpi)
                ?: displayMetrics.densityDpi.toFloat().takeIf(::isPlausibleDpi)
                ?: BASELINE_DENSITY_DPI.toFloat()
            val verticalDpi = displayMetrics.ydpi.takeIf(::isPlausibleDpi)
                ?: displayMetrics.densityDpi.toFloat().takeIf(::isPlausibleDpi)
                ?: BASELINE_DENSITY_DPI.toFloat()
            return DeviceDistanceConfig(
                horizontalPixelsPerMeter = horizontalDpi * INCHES_PER_METER,
                verticalPixelsPerMeter = verticalDpi * INCHES_PER_METER,
            )
        }

        private fun isPlausibleDpi(value: Float): Boolean =
            value.isFinite() && value in MIN_PLAUSIBLE_DPI..MAX_PLAUSIBLE_DPI
    }
}

/** Direction refers to increasing scroll position, not the user's finger movement. */
object ScrollDirectionResolver {
    fun resolve(event: RawScrollEvent): ScrollDirection {
        val horizontal = event.deltaX?.toLong() ?: 0L
        val vertical = event.deltaY?.toLong() ?: 0L
        if (horizontal != 0L || vertical != 0L) {
            return if (abs(vertical) >= abs(horizontal)) {
                if (vertical > 0L) ScrollDirection.DOWN else ScrollDirection.UP
            } else {
                if (horizontal > 0L) ScrollDirection.RIGHT else ScrollDirection.LEFT
            }
        }

        val indexDelta = (event.toIndex?.toLong() ?: 0L) - (event.fromIndex?.toLong() ?: 0L)
        return when {
            indexDelta > 0L -> ScrollDirection.DOWN
            indexDelta < 0L -> ScrollDirection.UP
            else -> ScrollDirection.UNKNOWN
        }
    }
}
