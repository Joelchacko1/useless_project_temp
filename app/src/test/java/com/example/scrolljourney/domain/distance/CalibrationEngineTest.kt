package com.example.scrolljourney.domain.distance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalibrationEngineTest {
    private val engine = CalibrationEngine()

    @Test
    fun `uses median and rejects robust outlier`() {
        val result = engine.createProfile(
            profileId = "profile",
            measurementsMeters = listOf(0.19, 0.20, 0.21, 1.5),
            nowEpochMs = 100L,
        )

        requireNotNull(result)
        assertEquals(listOf(0.19, 0.20, 0.21), result.acceptedMeasurementsMeters)
        assertEquals(1, result.rejectedMeasurementCount)
        assertEquals(3, result.profile.sampleCount)
        assertEquals(0.20, result.profile.medianDistanceMeters, 0.0001)
        assertEquals(0.20, result.profile.meanDistanceMeters, 0.0001)
        assertEquals(0.20, result.robustDistanceMeters, 0.0001)
    }

    @Test
    fun `rejects non finite and out of range calibration input`() {
        val result = engine.createProfile(
            profileId = "profile",
            measurementsMeters = listOf(Double.NaN, 0.001, 6.0),
            nowEpochMs = 100L,
        )

        assertNull(result)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `requires an explicit profile identifier`() {
        engine.createProfile(
            profileId = " ",
            measurementsMeters = listOf(0.2),
            nowEpochMs = 100L,
        )
    }
}
