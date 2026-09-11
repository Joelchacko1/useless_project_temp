package com.example.scrolljourney.tracking

import android.view.accessibility.AccessibilityEvent
import com.example.scrolljourney.domain.distance.DeviceDistanceConfig
import com.example.scrolljourney.domain.distance.EstimationMethod
import com.example.scrolljourney.domain.distance.HybridDistanceCalculator
import com.example.scrolljourney.domain.tracking.InMemoryTrackingStatusController
import com.example.scrolljourney.domain.tracking.RawScrollEvent
import com.example.scrolljourney.domain.tracking.ScrollDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ScrollEventProcessorTest {
    private fun processor() = ScrollEventProcessor(
        HybridDistanceCalculator(
            DeviceDistanceConfig(
                horizontalPixelsPerMeter = 1_000.0,
                verticalPixelsPerMeter = 1_000.0,
            ),
        ),
    )

    @Test
    fun `filters non scroll event types`() {
        val result = processor().process(
            raw(eventType = AccessibilityEvent.TYPE_VIEW_CLICKED, deltaY = 100),
            calibrationProfile = null,
        )

        assertNull(result)
    }

    @Test
    fun `suppresses duplicate callbacks with identical numeric metadata`() {
        val processor = processor()
        val first = raw(id = "first", timestampEpochMs = 100L, deltaY = 80, scrollY = 80)
        val duplicate = raw(id = "second", timestampEpochMs = 200L, deltaY = 80, scrollY = 80)

        assertNotNull(processor.process(first, calibrationProfile = null))
        assertNull(processor.process(duplicate, calibrationProfile = null))
    }

    @Test
    fun `accepts position-changing callbacks from a continuous scroll`() {
        val processor = processor()

        assertNotNull(processor.process(raw(timestampEpochMs = 100L, deltaY = 50, scrollY = 50), null))
        assertNotNull(processor.process(raw(timestampEpochMs = 130L, deltaY = 50, scrollY = 100), null))
    }

    @Test
    fun `uses directional delta and preserves normalized package attribution`() {
        val result = processor().process(
            raw(packageName = "  com.example.reader  ", deltaY = -400),
            calibrationProfile = null,
        )

        requireNotNull(result)
        assertEquals("com.example.reader", result.packageName)
        assertEquals(ScrollDirection.UP, result.direction)
        assertEquals(EstimationMethod.ACTUAL_DELTA, result.estimationMethod)
        assertEquals(0.4, result.distanceMeters, 0.0001)
    }

    @Test
    fun `uses index direction and fallback when deltas are unavailable`() {
        val result = processor().process(
            raw(deltaY = null, fromIndex = 3, toIndex = 7),
            calibrationProfile = null,
        )

        requireNotNull(result)
        assertEquals(ScrollDirection.DOWN, result.direction)
        assertEquals(EstimationMethod.FALLBACK_ESTIMATE, result.estimationMethod)
    }

    @Test
    fun `drops zero delta events without independent movement evidence`() {
        val result = processor().process(raw(deltaX = 0, deltaY = 0), calibrationProfile = null)

        assertNull(result)
    }

    @Test
    fun `derives actual delta from changed absolute scroll position`() {
        val processor = processor()

        assertNull(processor.process(raw(deltaY = null, scrollY = 0, timestampEpochMs = 100L), null))
        val result = processor.process(raw(deltaY = null, scrollY = 120, timestampEpochMs = 160L), null)

        requireNotNull(result)
        assertEquals(ScrollDirection.DOWN, result.direction)
        assertEquals(EstimationMethod.ACTUAL_DELTA, result.estimationMethod)
        assertEquals(0.12, result.distanceMeters, 0.0001)
    }

    @Test
    fun `same timestamp only suppresses matching callback snapshots`() {
        val processor = processor()

        assertNotNull(processor.process(raw(id = "one", timestampEpochMs = 100L, deltaY = 30, scrollY = 30), null))
        val result = processor.process(raw(id = "two", timestampEpochMs = 100L, deltaY = 60, scrollY = 90), null)

        assertNotNull(result)
    }

    @Test
    fun `tracking state is active only when enabled and service connected`() {
        val controller = InMemoryTrackingStatusController()

        controller.setTrackingEnabled(true, nowEpochMs = 100L)
        assertNull(controller.trackingState.value.activeSinceEpochMs)

        controller.setServiceConnected(true, nowEpochMs = 200L)
        assertEquals(200L, controller.trackingState.value.activeSinceEpochMs)

        controller.setServiceConnected(false, nowEpochMs = 300L)
        assertNull(controller.trackingState.value.activeSinceEpochMs)
    }

    private fun raw(
        id: String = "event",
        timestampEpochMs: Long = 100L,
        packageName: String = "com.example.reader",
        eventType: Int = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        deltaX: Int? = null,
        deltaY: Int? = 100,
        fromIndex: Int? = null,
        toIndex: Int? = null,
        itemCount: Int? = null,
        scrollX: Int? = null,
        scrollY: Int? = null,
        maxScrollX: Int? = null,
        maxScrollY: Int? = null,
    ) = RawScrollEvent(
        id = id,
        timestampEpochMs = timestampEpochMs,
        packageName = packageName,
        eventType = eventType,
        deltaX = deltaX,
        deltaY = deltaY,
        fromIndex = fromIndex,
        toIndex = toIndex,
        itemCount = itemCount,
        scrollX = scrollX,
        scrollY = scrollY,
        maxScrollX = maxScrollX,
        maxScrollY = maxScrollY,
    )
}
