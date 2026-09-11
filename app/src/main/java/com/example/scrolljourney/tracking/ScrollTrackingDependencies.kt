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
    @Volatile
    private var current = ScrollTrackingDependencySnapshot(
        scrollEventSink = NoOpScrollEventSink,
        calibrationProfileProvider = EmptyCalibrationProfileProvider,
        trackingStatusController = InMemoryTrackingStatusController(),
    )

    fun configure(
        scrollEventSink: ScrollEventSink,
        calibrationProfileProvider: CalibrationProfileProvider,
        trackingStatusController: TrackingStatusController,
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
