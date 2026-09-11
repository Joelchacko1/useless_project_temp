package com.example.scrolljourney.tracking

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.scrolljourney.domain.distance.DeviceDistanceConfig
import com.example.scrolljourney.domain.distance.HybridDistanceCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * Android entry point for numeric scroll-event ingestion. Its manifest declaration and service
 * configuration are intentionally left to Agent 4, who owns the protected integration files.
 */
class ScrollAccessibilityService : AccessibilityService() {
    private val eventNormalizer = AccessibilityScrollEventNormalizer()
    private lateinit var eventProcessor: ScrollEventProcessor
    private var processingDispatcher: ExecutorCoroutineDispatcher? = null
    private var processingScope: CoroutineScope? = null
    private var calibrationProfileLoaded = false
    private var calibrationProfile: com.example.scrolljourney.domain.distance.CalibrationProfile? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        eventProcessor = ScrollEventProcessor(
            HybridDistanceCalculator(DeviceDistanceConfig.fromDisplayMetrics(resources.displayMetrics)),
        )
        val dispatcher = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "ScrollJourneyTracking").apply { isDaemon = true }
        }.asCoroutineDispatcher()
        processingDispatcher = dispatcher
        processingScope = CoroutineScope(SupervisorJob() + dispatcher)

        val dependencies = ScrollTrackingDependencies.snapshot()
        Log.d(
            DEBUG_TAG,
            "onServiceConnected: process=${android.os.Process.myPid()} " +
                "trackingStateBeforeConnect=${dependencies.trackingStatusController.trackingState.value} " +
                "sink=${dependencies.scrollEventSink::class.simpleName}",
        )
        dependencies.trackingStatusController.setServiceConnected(true)
        processingScope?.launch {
            try {
                calibrationProfile = dependencies.calibrationProfileProvider.activeProfile()
                calibrationProfileLoaded = true
            } catch (error: Exception) {
                calibrationProfileLoaded = true
                Log.e(LOG_TAG, "Could not load calibration profile", error)
            }
        }
        Log.d(LOG_TAG, "Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(
            DEBUG_TAG,
            "onAccessibilityEvent type=${AccessibilityEvent.eventTypeToString(event.eventType)} " +
                "package=${event.packageName}",
        )
        val dependencies = ScrollTrackingDependencies.snapshot()
        if (!dependencies.trackingStatusController.trackingState.value.isTrackingEnabled) {
            Log.d(
                DEBUG_TAG,
                "Dropped: tracking disabled, trackingState=${dependencies.trackingStatusController.trackingState.value}",
            )
            return
        }
        val rawEvent = eventNormalizer.normalize(event)
        if (rawEvent == null) {
            Log.d(DEBUG_TAG, "Dropped: normalizer returned null for eventType=${event.eventType}")
            return
        }
        Log.d(
            DEBUG_TAG,
            "Normalized package=${rawEvent.packageName} deltaX=${rawEvent.deltaX} deltaY=${rawEvent.deltaY} " +
                "scrollX=${rawEvent.scrollX} scrollY=${rawEvent.scrollY} fromIndex=${rawEvent.fromIndex} " +
                "toIndex=${rawEvent.toIndex} timestampEpochMs=${rawEvent.timestampEpochMs}",
        )
        val scope = processingScope
        if (scope == null) {
            Log.d(DEBUG_TAG, "Dropped: processingScope is null (service not fully connected)")
            return
        }
        if (!::eventProcessor.isInitialized) {
            Log.d(DEBUG_TAG, "Dropped: eventProcessor not initialized")
            return
        }

        scope.launch {
            if (!dependencies.trackingStatusController.trackingState.value.isTrackingEnabled) return@launch
            try {
                val processed = eventProcessor.process(rawEvent, calibrationProfile)
                if (processed != null) {
                    dependencies.scrollEventSink.recordScroll(processed)
                    Log.d(
                        LOG_TAG,
                        "Recorded scroll package=${processed.packageName} direction=${processed.direction} " +
                            "method=${processed.estimationMethod} meters=${processed.distanceMeters}",
                    )
                } else {
                    Log.d(DEBUG_TAG, "Dropped: processor returned null for package=${rawEvent.packageName}")
                }
            } catch (error: Exception) {
                Log.e(
                    LOG_TAG,
                    "Failed to process numeric scroll metadata for package=${rawEvent.packageName}",
                    error,
                )
            }
        }
    }

    override fun onInterrupt() {
        Log.w(LOG_TAG, "Accessibility service interrupted")
    }

    override fun onDestroy() {
        ScrollTrackingDependencies.snapshot().trackingStatusController.setServiceConnected(false)
        processingScope?.cancel()
        processingDispatcher?.close()
        processingScope = null
        processingDispatcher = null
        super.onDestroy()
    }

    companion object {
        private const val LOG_TAG = "ScrollJourney"

        /** TEMPORARY diagnostic tag for pipeline tracing; safe to remove once tracking is verified. */
        private const val DEBUG_TAG = "ScrollJourneyDebug"
    }
}
