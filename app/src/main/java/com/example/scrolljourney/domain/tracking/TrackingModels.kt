package com.example.scrolljourney.domain.tracking

import com.example.scrolljourney.domain.distance.EstimationMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * The tracking state shared with UI and integration code.
 *
 * Enabling tracking and connecting the Android accessibility service are separate states: a
 * user may enable the in-app preference before Android has connected the service.
 */
data class TrackingState(
    val isTrackingEnabled: Boolean,
    val serviceConnected: Boolean,
    val activeSinceEpochMs: Long?,
)

/**
 * Privacy-preserving snapshot of the numeric fields needed to estimate one accessibility scroll.
 * No accessibility-node text or screen content is represented here.
 */
data class RawScrollEvent(
    val id: String,
    val timestampEpochMs: Long,
    val packageName: String,
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

@Serializable
enum class ScrollDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    UNKNOWN,
}

/** The record published by Agent 1 and persisted by the data implementation. */
@Serializable
data class ProcessedScroll(
    val id: String,
    val timestampEpochMs: Long,
    val packageName: String,
    val direction: ScrollDirection,
    val distanceMeters: Double,
    val confidence: Float,
    val estimationMethod: EstimationMethod,
)

/** Implemented by the data layer; the accessibility service never depends on Room directly. */
interface ScrollEventSink {
    suspend fun recordScroll(event: ProcessedScroll)
}

/** Read-only state contract consumed by UI and integration code. */
interface TrackingStatusProvider {
    val trackingState: StateFlow<TrackingState>
}

/**
 * Mutable half of [TrackingStatusProvider], normally retained by app-level integration code.
 * Agent 4 can replace this in-memory implementation with a preference-backed adapter.
 */
interface TrackingStatusController : TrackingStatusProvider {
    fun setTrackingEnabled(enabled: Boolean, nowEpochMs: Long = System.currentTimeMillis())

    fun setServiceConnected(connected: Boolean, nowEpochMs: Long = System.currentTimeMillis())
}

/**
 * Process-local tracking state suitable for the initial integration. It intentionally does not
 * persist the user preference; preference ownership belongs to the data/integration lanes.
 */
class InMemoryTrackingStatusController(
    initiallyEnabled: Boolean = false,
) : TrackingStatusController {
    private val mutableTrackingState = MutableStateFlow(
        TrackingState(
            isTrackingEnabled = initiallyEnabled,
            serviceConnected = false,
            activeSinceEpochMs = null,
        ),
    )

    override val trackingState: StateFlow<TrackingState> = mutableTrackingState.asStateFlow()

    override fun setTrackingEnabled(enabled: Boolean, nowEpochMs: Long) {
        updateState(enabled, mutableTrackingState.value.serviceConnected, nowEpochMs)
    }

    override fun setServiceConnected(connected: Boolean, nowEpochMs: Long) {
        updateState(mutableTrackingState.value.isTrackingEnabled, connected, nowEpochMs)
    }

    private fun updateState(enabled: Boolean, connected: Boolean, nowEpochMs: Long) {
        val current = mutableTrackingState.value
        val shouldBeActive = enabled && connected
        mutableTrackingState.value = TrackingState(
            isTrackingEnabled = enabled,
            serviceConnected = connected,
            activeSinceEpochMs = when {
                !shouldBeActive -> null
                current.activeSinceEpochMs != null -> current.activeSinceEpochMs
                else -> nowEpochMs
            },
        )
    }
}
