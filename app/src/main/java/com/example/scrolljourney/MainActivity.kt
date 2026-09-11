package com.example.scrolljourney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.scrolljourney.data.repository.PersistentScrollRepository
import com.example.scrolljourney.domain.distance.EmptyCalibrationProfileProvider
import com.example.scrolljourney.integration.CalibrationRepositoryStub
import com.example.scrolljourney.integration.GamificationRepositoryBridge
import com.example.scrolljourney.integration.RealTrackingController
import com.example.scrolljourney.integration.ScrollStatsRepositoryBridge
import com.example.scrolljourney.tracking.ScrollTrackingDependencies
import com.example.scrolljourney.ui.navigation.ScrollJourneyNavigation
import com.example.scrolljourney.ui.state.ScrollJourneyViewModel
import com.example.scrolljourney.ui.theme.ScrollJourneyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var trackingController: RealTrackingController
    private lateinit var scrollRepository: PersistentScrollRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scrollRepository = ScrollTrackingDependencies.scrollRepository(applicationContext.filesDir)

        ScrollTrackingDependencies.configure(
            scrollEventSink = scrollRepository,
            calibrationProfileProvider = EmptyCalibrationProfileProvider,
        )

        trackingController = RealTrackingController(this, ScrollTrackingDependencies.trackingStatusController)

        val statsBridge = ScrollStatsRepositoryBridge(scrollRepository)
        val gamificationBridge = GamificationRepositoryBridge(scrollRepository)

        enableEdgeToEdge()
        setContent {
            ScrollJourneyTheme {
                val viewModel = remember {
                    ScrollJourneyViewModel(
                        statsRepository = statsBridge,
                        trackingController = trackingController,
                        gamificationRepository = gamificationBridge,
                    )
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    ScrollJourneyNavigation(
                        viewModel = viewModel,
                        onOpenAccessibilitySettings = {
                            trackingController.openAccessibilitySettings()
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::trackingController.isInitialized) {
            trackingController.refreshAccessibilityStatus()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::scrollRepository.isInitialized) {
            lifecycleScope.launch { scrollRepository.flush() }
        }
    }
}
