package com.example.scrolljourney.domain.distance

import android.view.accessibility.AccessibilityEvent
import com.example.scrolljourney.domain.tracking.RawScrollEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class HybridDistanceCalculatorTest {
    private val calculator = HybridDistanceCalculator(
        DeviceDistanceConfig(
            horizontalPixelsPerMeter = 1_000.0,
            verticalPixelsPerMeter = 1_000.0,
            fallbackScrollDistanceMeters = 0.12,
        ),
    )

    @Test
    fun `uses reliable numeric delta as actual distance`() {
        val result = calculator.estimate(raw(deltaY = -500), calibrationProfile = null)

        assertEquals(0.5, result.distanceMeters, 0.0001)
        assertEquals(0.90f, result.confidence, 0.0001f)
        assertEquals(EstimationMethod.ACTUAL_DELTA, result.estimationMethod)
    }

    @Test
    fun `uses robust calibration median when delta is unavailable`() {
        val result = calculator.estimate(
            raw(deltaY = null),
            calibrationProfile = CalibrationProfile(
                id = "profile",
                createdAtEpochMs = 1L,
                sampleCount = 10,
                medianDistanceMeters = 0.31,
                meanDistanceMeters = 0.33,
                lastUpdatedEpochMs = 2L,
            ),
        )

        assertEquals(0.31, result.distanceMeters, 0.0001)
        assertEquals(0.675f, result.confidence, 0.0001f)
        assertEquals(EstimationMethod.CALIBRATED_ESTIMATE, result.estimationMethod)
    }

    @Test
    fun `uses documented low confidence fallback without calibration`() {
        val result = calculator.estimate(raw(deltaY = null), calibrationProfile = null)

        assertEquals(0.12, result.distanceMeters, 0.0001)
        assertEquals(0.25f, result.confidence, 0.0001f)
        assertEquals(EstimationMethod.FALLBACK_ESTIMATE, result.estimationMethod)
    }

    @Test
    fun `falls back from malformed huge delta to calibration`() {
        val result = calculator.estimate(
            raw(deltaY = 25_000),
            calibrationProfile = CalibrationProfile(
                id = "profile",
                createdAtEpochMs = 1L,
                sampleCount = 1,
                medianDistanceMeters = 0.2,
                meanDistanceMeters = 0.2,
                lastUpdatedEpochMs = 1L,
            ),
        )

        assertEquals(0.2, result.distanceMeters, 0.0001)
        assertEquals(EstimationMethod.CALIBRATED_ESTIMATE, result.estimationMethod)
    }

    private fun raw(deltaY: Int?): RawScrollEvent = RawScrollEvent(
        id = "event",
        timestampEpochMs = 100L,
        packageName = "com.example.reader",
        eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        deltaX = null,
        deltaY = deltaY,
        fromIndex = null,
        toIndex = null,
        itemCount = null,
        scrollX = null,
        scrollY = null,
        maxScrollX = null,
        maxScrollY = null,
    )
}
