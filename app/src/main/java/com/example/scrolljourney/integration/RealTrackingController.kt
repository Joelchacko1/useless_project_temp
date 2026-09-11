package com.example.scrolljourney.integration

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.example.scrolljourney.domain.repository.TrackingController
import com.example.scrolljourney.domain.tracking.TrackingStatusController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class RealTrackingController(
    private val context: Context,
    private val trackingStatusController: TrackingStatusController,
) : TrackingController {

    private val _accessibilityPermissionGranted = MutableStateFlow(checkAccessibilityEnabled())
    private val _overlayPermissionGranted = MutableStateFlow(checkOverlayPermissionGranted())

    override fun isTrackingEnabled(): Flow<Boolean> =
        trackingStatusController.trackingState.map { it.isTrackingEnabled }

    override suspend fun setTrackingEnabled(enabled: Boolean) {
        trackingStatusController.setTrackingEnabled(enabled)
    }

    override fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun isServiceConnected(): Flow<Boolean> =
        trackingStatusController.trackingState.map { it.serviceConnected }

    override fun isAccessibilityPermissionGranted(): Flow<Boolean> =
        _accessibilityPermissionGranted

    override fun isOverlayPermissionGranted(): Flow<Boolean> =
        _overlayPermissionGranted

    override fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}"),
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun refreshAccessibilityStatus() {
        _accessibilityPermissionGranted.value = checkAccessibilityEnabled()
    }

    fun refreshOverlayPermissionStatus() {
        _overlayPermissionGranted.value = checkOverlayPermissionGranted()
    }

    private fun checkAccessibilityEnabled(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_GENERIC,
        )
        val packageName = context.packageName
        return enabledServices.any { info ->
            info.resolveInfo.serviceInfo.packageName == packageName
        }
    }

    private fun checkOverlayPermissionGranted(): Boolean = Settings.canDrawOverlays(context)
}
