package com.example.scrolljourney.tracking

import android.view.accessibility.AccessibilityEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccessibilityScrollEventNormalizerTest {
    @Test
    fun `normalizes numeric metadata and trims package attribution`() {
        val normalizer = AccessibilityScrollEventNormalizer(
            nowEpochMs = { 999L },
            idGenerator = { "event-id" },
        )

        val result = normalizer.normalizeSnapshot(
            snapshot(
                packageName = "  com.example.reader  ",
                deltaY = 48,
                scrollY = 240,
            ),
        )

        requireNotNull(result)
        assertEquals("event-id", result.id)
        assertEquals("com.example.reader", result.packageName)
        assertEquals(48, result.deltaY)
        assertEquals(240, result.scrollY)
    }

    @Test
    fun `uses current clock for framework events with no timestamp`() {
        val normalizer = AccessibilityScrollEventNormalizer(
            nowEpochMs = { 999L },
            idGenerator = { "event-id" },
        )

        val result = normalizer.normalizeSnapshot(snapshot(timestampEpochMs = 0L))

        requireNotNull(result)
        assertEquals(999L, result.timestampEpochMs)
    }

    @Test
    fun `filters non scroll event types`() {
        val normalizer = AccessibilityScrollEventNormalizer(idGenerator = { "event-id" })

        val result = normalizer.normalizeSnapshot(
            snapshot(eventType = AccessibilityEvent.TYPE_VIEW_CLICKED),
        )

        assertNull(result)
    }

    private fun snapshot(
        timestampEpochMs: Long = 100L,
        packageName: String? = "com.example.reader",
        eventType: Int = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        deltaX: Int? = null,
        deltaY: Int? = null,
        fromIndex: Int? = null,
        toIndex: Int? = null,
        itemCount: Int? = null,
        scrollX: Int? = null,
        scrollY: Int? = null,
        maxScrollX: Int? = null,
        maxScrollY: Int? = null,
    ) = AccessibilityScrollEventSnapshot(
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
