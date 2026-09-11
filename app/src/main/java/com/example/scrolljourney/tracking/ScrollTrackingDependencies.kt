package com.example.scrolljourney.tracking

import com.example.scrolljourney.domain.distance.CalibrationProfileProvider
import com.example.scrolljourney.domain.distance.EmptyCalibrationProfileProvider
import com.example.scrolljourney.domain.tracking.InMemoryTrackingStatusController
import com.example.scrolljourney.domain.tracking.ScrollEventSink
import com.example.scrolljourney.domain.tracking.TrackingStatusController

/**
 * Narrow app-level bridge for the system-constructed accessibility service.
 * Agent 4 configures it during app startup with Agent 3's persistence implementations.
 */
object ScrollTrackingDependencies {
    /**
     * Owned here (not passed in via [configure]) so it survives MainActivity being
     * recreated within the same process — e.g. after a configuration change, or after
     * the accessibility service already connected once. If a caller replaced this
     * instance on every configure(), the service's earlier setServiceConnected(true)
     * call would be lost and the UI's "service connected" status would read stale
     * forever, even though the OS still has the service bound.
     */
    val trackingStatusController: TrackingStatusController = InMemoryTrackingStatusController()

    @Volatile
    private var current = ScrollTrackingDependencySnapshot(
        scrollEventSink = NoOpScrollEventSink,
        calibrationProfileProvider = EmptyCalibrationProfileProvider,
        trackingStatusController = trackingStatusController,
    )

    fun configure(
        scrollEventSink: ScrollEventSink,
        calibrationProfileProvider: CalibrationProfileProvider,
    ) {
        current = ScrollTrackingDependencySnapshot(
            scrollEventSink = scrollEventSink,
            calibrationProfileProvider = calibrationProfileProvider,
            trackingStatusController = trackingStatusController,
        )
    }

    fun snapshot(): ScrollTrackingDependencySnapshot = current
}

data class ScrollTrackingDependencySnapshot(
    val scrollEventSink: ScrollEventSink,
    val calibrationProfileProvider: CalibrationProfileProvider,
    val trackingStatusController: TrackingStatusController,
)

private object NoOpScrollEventSink : ScrollEventSink {
    override suspend fun recordScroll(event: com.example.scrolljourney.domain.tracking.ProcessedScroll) = Unit
}
