package com.example.scrolljourney.tracking

import android.os.Build
import android.view.accessibility.AccessibilityEvent
import com.example.scrolljourney.domain.tracking.RawScrollEvent
import java.util.UUID

/**
 * Extracts only the numeric scroll metadata needed by ScrollJourney from an accessibility event.
 * This class deliberately never requests or reads an AccessibilityNodeInfo or node text.
 */
class AccessibilityScrollEventNormalizer(
    private val nowEpochMs: () -> Long = System::currentTimeMillis,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) {
    fun normalize(event: AccessibilityEvent): RawScrollEvent? {
        if (event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) return null
        return normalizeSnapshot(
            AccessibilityScrollEventSnapshot(
                timestampEpochMs = event.eventTime,
                packageName = event.packageName?.toString(),
                eventType = event.eventType,
                deltaX = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) event.scrollDeltaX else null,
                deltaY = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) event.scrollDeltaY else null,
                fromIndex = event.fromIndex.knownNonNegative(),
                toIndex = event.toIndex.knownNonNegative(),
                itemCount = event.itemCount.knownNonNegative(),
                scrollX = event.scrollX.knownNonNegative(),
                scrollY = event.scrollY.knownNonNegative(),
                maxScrollX = event.maxScrollX.knownNonNegative(),
                maxScrollY = event.maxScrollY.knownNonNegative(),
            ),
        )
    }

    internal fun normalizeSnapshot(snapshot: AccessibilityScrollEventSnapshot): RawScrollEvent? {
        if (snapshot.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) return null
        return RawScrollEvent(
            id = idGenerator(),
            timestampEpochMs = snapshot.timestampEpochMs.takeIf { it > 0L } ?: nowEpochMs(),
            packageName = snapshot.packageName?.trim().orEmpty(),
            eventType = snapshot.eventType,
            deltaX = snapshot.deltaX,
            deltaY = snapshot.deltaY,
            fromIndex = snapshot.fromIndex,
            toIndex = snapshot.toIndex,
            itemCount = snapshot.itemCount,
            scrollX = snapshot.scrollX,
            scrollY = snapshot.scrollY,
            maxScrollX = snapshot.maxScrollX,
            maxScrollY = snapshot.maxScrollY,
        )
    }

    private fun Int.knownNonNegative(): Int? = takeIf { it >= 0 }
}

/** Pure input shape used to test normalisation without a device framework event. */
internal data class AccessibilityScrollEventSnapshot(
    val timestampEpochMs: Long,
    val packageName: String?,
    val eventType: Int,
    val deltaX: Int?,
    val deltaY: Int?,
    val fromIndex: Int?,
    val toIndex: Int?,
    val itemCount: Int?,
    val scrollX: Int?,
    val scrollY: Int?,
    val maxScrollX: Int?,
    val maxScrollY: Int?,
)
