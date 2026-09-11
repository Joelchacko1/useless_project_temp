package com.example.scrolljourney.tracking

import android.view.accessibility.AccessibilityEvent
import com.example.scrolljourney.domain.distance.CalibrationProfile
import com.example.scrolljourney.domain.distance.HybridDistanceCalculator
import com.example.scrolljourney.domain.distance.ScrollDirectionResolver
import com.example.scrolljourney.domain.tracking.ProcessedScroll
import com.example.scrolljourney.domain.tracking.RawScrollEvent
import com.example.scrolljourney.domain.tracking.ScrollDirection
import kotlin.math.abs

/**
 * Stateful, single-threaded event processor that rejects non-scroll/noise events and repeat
 * callbacks before creating one persistable measurement. Call it from one serial dispatcher.
 */
class ScrollEventProcessor(
    private val distanceCalculator: HybridDistanceCalculator,
) {
    private val sourceStates = LinkedHashMap<String, SourceState>(INITIAL_SOURCE_CAPACITY, 0.75f, true)

    fun process(
        rawEvent: RawScrollEvent,
        calibrationProfile: CalibrationProfile?,
    ): ProcessedScroll? {
        if (rawEvent.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) return null
        val packageName = rawEvent.packageName.trim()
        if (packageName.isEmpty() || rawEvent.timestampEpochMs < 0L) return null

        val state = stateFor(packageName)
        val previousObserved = state.lastObserved
        if (previousObserved != null && rawEvent.timestampEpochMs < previousObserved.timestampEpochMs) {
            return null
        }

        val normalizedEvent = rawEvent.copy(packageName = packageName)
        val effectiveEvent = normalizedEvent.withPositionDerivedDeltas(previousObserved)
        state.lastObserved = normalizedEvent

        val direction = ScrollDirectionResolver.resolve(effectiveEvent)
        if (direction == ScrollDirection.UNKNOWN) return null
        if (state.lastAccepted?.isDuplicateOf(effectiveEvent) == true) return null

        val estimate = distanceCalculator.estimate(effectiveEvent, calibrationProfile)
        val processed = ProcessedScroll(
            id = effectiveEvent.id,
            timestampEpochMs = effectiveEvent.timestampEpochMs,
            packageName = effectiveEvent.packageName,
            direction = direction,
            distanceMeters = estimate.distanceMeters,
            confidence = estimate.confidence,
            estimationMethod = estimate.estimationMethod,
        )
        state.lastAccepted = effectiveEvent
        return processed
    }

    private fun stateFor(packageName: String): SourceState {
        sourceStates[packageName]?.let { return it }
        if (sourceStates.size >= MAX_TRACKED_SOURCES) {
            sourceStates.entries.iterator().apply {
                if (hasNext()) {
                    next()
                    remove()
                }
            }
        }
        return SourceState().also { sourceStates[packageName] = it }
    }

    private fun RawScrollEvent.withPositionDerivedDeltas(previous: RawScrollEvent?): RawScrollEvent {
        if (previous == null) return this
        val derivedDeltaX = positionDifference(scrollX, previous.scrollX)
        val derivedDeltaY = positionDifference(scrollY, previous.scrollY)
        return copy(
            deltaX = deltaX.takeUnless { it == null || it == 0 } ?: derivedDeltaX,
            deltaY = deltaY.takeUnless { it == null || it == 0 } ?: derivedDeltaY,
        )
    }

    private fun positionDifference(current: Int?, previous: Int?): Int? {
        if (current == null || previous == null) return null
        val difference = current.toLong() - previous.toLong()
        return difference.takeIf { it in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() }?.toInt()
    }

    private fun RawScrollEvent.isDuplicateOf(current: RawScrollEvent): Boolean {
        val elapsedMs = current.timestampEpochMs - timestampEpochMs
        return elapsedMs in 0L..DUPLICATE_CALLBACK_WINDOW_MS &&
            deltaX == current.deltaX &&
            deltaY == current.deltaY &&
            fromIndex == current.fromIndex &&
            toIndex == current.toIndex &&
            itemCount == current.itemCount &&
            scrollX == current.scrollX &&
            scrollY == current.scrollY &&
            maxScrollX == current.maxScrollX &&
            maxScrollY == current.maxScrollY
    }

    private data class SourceState(
        var lastObserved: RawScrollEvent? = null,
        var lastAccepted: RawScrollEvent? = null,
    )

    companion object {
        private const val INITIAL_SOURCE_CAPACITY = 16

        /** Bounds memory for long-lived services while retaining recent per-app callback state. */
        const val MAX_TRACKED_SOURCES = 128

        /**
         * Repeated callbacks with identical numeric metadata inside this window are duplicates,
         * not separate scrolls. Position-changing callbacks remain distinct measurements.
         */
        const val DUPLICATE_CALLBACK_WINDOW_MS = 250L
    }
}
