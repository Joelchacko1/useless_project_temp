package com.example.scrolljourney.tracking

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.scrolljourney.domain.tracking.TrackingState
import com.example.scrolljourney.domain.tracking.TrackingStatusController
import com.example.scrolljourney.domain.tracking.nextTrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [TrackingStatusController] backed by [SharedPreferences], so the user's tracking-enabled
 * choice survives the app being closed instead of resetting to off every launch. Loaded
 * synchronously at construction since it's a single boolean — no async disk read needed.
 */
class PersistentTrackingStatusController(
    private val prefs: SharedPreferences,
) : TrackingStatusController {
    private val mutableTrackingState = MutableStateFlow(
        TrackingState(
            isTrackingEnabled = prefs.getBoolean(KEY_TRACKING_ENABLED, false),
            serviceConnected = false,
            activeSinceEpochMs = null,
        ),
    )

    override val trackingState: StateFlow<TrackingState> = mutableTrackingState.asStateFlow()

    override fun setTrackingEnabled(enabled: Boolean, nowEpochMs: Long) {
        prefs.edit { putBoolean(KEY_TRACKING_ENABLED, enabled) }
        mutableTrackingState.value = nextTrackingState(
            mutableTrackingState.value, enabled, mutableTrackingState.value.serviceConnected, nowEpochMs,
        )
    }

    override fun setServiceConnected(connected: Boolean, nowEpochMs: Long) {
        mutableTrackingState.value = nextTrackingState(
            mutableTrackingState.value, mutableTrackingState.value.isTrackingEnabled, connected, nowEpochMs,
        )
    }

    companion object {
        private const val KEY_TRACKING_ENABLED = "tracking_enabled"
    }
}
