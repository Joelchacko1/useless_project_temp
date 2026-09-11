package com.example.scrolljourney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.scrolljourney.data.mock.MockCalibrationRepository
import com.example.scrolljourney.data.mock.MockGamificationRepository
import com.example.scrolljourney.data.mock.MockScrollStatsRepository
import com.example.scrolljourney.data.mock.MockTrackingController
import com.example.scrolljourney.ui.navigation.ScrollJourneyNavigation
import com.example.scrolljourney.ui.state.ScrollJourneyViewModel
import com.example.scrolljourney.ui.theme.ScrollJourneyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScrollJourneyTheme {
                // TODO: Agent 4 to inject real repositories from integration layer
                val viewModel = remember {
                    ScrollJourneyViewModel(
                        statsRepository = MockScrollStatsRepository(),
                        trackingController = MockTrackingController(),
                        gamificationRepository = MockGamificationRepository()
                    )
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    ScrollJourneyNavigation(
                        viewModel = viewModel,
                        onOpenAccessibilitySettings = {
                            // TODO: Agent 4 to implement accessibility settings navigation
                        }
                    )
                }
            }
        }
    }
}