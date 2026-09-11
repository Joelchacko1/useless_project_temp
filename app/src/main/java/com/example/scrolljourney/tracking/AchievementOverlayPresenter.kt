package com.example.scrolljourney.tracking

import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.example.scrolljourney.R
import com.example.scrolljourney.gamification.Achievement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Shows a small celebratory banner over whatever app is currently in the foreground when an
 * achievement unlocks, using a [WindowManager] overlay window — the only way to draw over other
 * apps from a background [android.accessibilityservice.AccessibilityService], since there's no
 * Activity to host a normal in-app dialog. Requires the user to have granted the
 * `SYSTEM_ALERT_WINDOW` ("draw over other apps") permission; silently does nothing without it.
 *
 * Built with plain Android views rather than a ComposeView: a `WindowManager` overlay is added
 * outside any Activity/Compose tree, so hosting Compose here would need a hand-rolled
 * LifecycleOwner/SavedStateRegistryOwner — not worth the complexity for one small banner.
 */
class AchievementOverlayPresenter(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var currentView: View? = null

    /**
     * Shows [achievement]'s banner and suspends until it auto-dismisses. Calling this from a
     * single sequential collector (as [ScrollAccessibilityService] does) naturally queues
     * achievements unlocked close together, showing them one at a time instead of overlapping.
     */
    suspend fun show(achievement: Achievement) {
        if (!Settings.canDrawOverlays(context)) return

        withContext(Dispatchers.Main.immediate) {
            addView(achievement)
        }
        delay(DISPLAY_DURATION_MS)
        withContext(Dispatchers.Main.immediate) {
            removeCurrentView()
        }
    }

    /** Removes any currently-shown banner. Call when the hosting service is destroyed. */
    fun dismissAll() {
        removeCurrentView()
    }

    private fun addView(achievement: Achievement) {
        removeCurrentView()
        try {
            val view = LayoutInflater.from(context).inflate(R.layout.view_achievement_overlay, null)
            view.findViewById<TextView>(R.id.achievement_overlay_title).text = achievement.title
            view.findViewById<TextView>(R.id.achievement_overlay_description).text = achievement.description
            view.setOnClickListener { removeCurrentView() }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT,
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = (context.resources.displayMetrics.density * TOP_MARGIN_DP).toInt()
            }

            windowManager.addView(view, params)
            currentView = view
        } catch (error: Exception) {
            Log.e(LOG_TAG, "Failed to show achievement overlay", error)
        }
    }

    private fun removeCurrentView() {
        currentView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (error: Exception) {
                Log.e(LOG_TAG, "Failed to remove achievement overlay", error)
            }
        }
        currentView = null
    }

    companion object {
        private const val LOG_TAG = "ScrollJourney"
        private const val DISPLAY_DURATION_MS = 3500L
        private const val TOP_MARGIN_DP = 48
    }
}
