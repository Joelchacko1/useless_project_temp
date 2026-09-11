package com.example.scrolljourney.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrolljourney.domain.ui.CalibrationUiState
import com.example.scrolljourney.domain.ui.PrivacyUiState
import com.example.scrolljourney.ui.components.NeoButton
import com.example.scrolljourney.ui.components.NeoPanel
import com.example.scrolljourney.ui.components.NeoTopBar
import com.example.scrolljourney.ui.theme.LocalScrollJourneyColors
import com.example.scrolljourney.ui.theme.ScrollJourneyTheme

@Composable
fun CalibrationScreen(
    state: CalibrationUiState,
    onStartCalibration: () -> Unit = {},
    onCancelCalibration: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NeoTopBar(
            title = "Calibration",
            fillColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                CalibrationInfoCard()
            }

            item {
                CalibrationProgressCard(state)
            }

            item {
                if (!state.isRunning && !state.isCompleted) {
                    NeoButton(
                        onClick = onStartCalibration,
                        fillColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Calibration", fontWeight = FontWeight.Bold)
                    }
                }

                if (state.isRunning) {
                    NeoButton(
                        onClick = onCancelCalibration,
                        fillColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Calibration")
                    }
                }

                if (state.isCompleted && state.error == null) {
                    NeoButton(
                        onClick = onStartCalibration,
                        fillColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Run Again", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (state.isCompleted && state.error == null) {
                item {
                    CalibrationResultsCard(state)
                }
            }

            if (state.error != null) {
                item {
                    NeoPanel(
                        fillColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.error,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CalibrationInfoCard(modifier: Modifier = Modifier) {
    val extra = LocalScrollJourneyColors.current
    NeoPanel(
        fillColor = extra.reward,
        contentColor = extra.onReward,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "What is Calibration?",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = extra.onReward,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Calibration helps estimate scroll distance more accurately for scrolls we can't measure directly. Just scroll normally in this app or any other for a few seconds — we'll learn a baseline from the scrolls we can measure precisely.",
            fontSize = 12.sp,
            color = extra.onReward,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun CalibrationProgressCard(
    state: CalibrationUiState,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp),
                    color = extra.reward
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Listening for scrolls...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }

            if (state.isCompleted && state.error == null) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = extra.success
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Calibration Complete!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Samples",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f),
                    )

                    Text(
                        text = "${state.sampleCount} / ${state.targetSampleCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }

                LinearProgressIndicator(
                    progress = { (state.sampleCount.toFloat() / state.targetSampleCount).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = extra.reward,
                    trackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun CalibrationResultsCard(
    state: CalibrationUiState,
    modifier: Modifier = Modifier
) {
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Calibration Results",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Median Distance",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                )
                Text(
                    text = "%.2f m".format(state.medianDistanceMeters),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Mean Distance",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                )
                Text(
                    text = "%.2f m".format(state.meanDistanceMeters),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
fun PrivacyScreen(
    state: PrivacyUiState,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NeoTopBar(
            title = "Privacy & Settings",
            fillColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                NeoPanel(
                    fillColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Local Storage Only",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "All your scroll data is stored locally on your device. Nothing is sent to external servers.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        lineHeight = 18.sp
                    )
                }
            }

            item {
                NeoPanel(
                    fillColor = extra.reward,
                    contentColor = extra.onReward,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Data Collected",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = extra.onReward,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    state.collectedData.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "•", fontSize = 12.sp, color = extra.onReward, modifier = Modifier.padding(end = 8.dp))
                            Text(text = item, fontSize = 12.sp, color = extra.onReward)
                        }
                    }
                }
            }

            item {
                NeoPanel(
                    fillColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Never Collected",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    state.notCollectedData.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "✓", fontSize = 12.sp, color = extra.success, modifier = Modifier.padding(end = 8.dp))
                            Text(text = item, fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiary)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalibrationScreenPreview() {
    ScrollJourneyTheme {
        CalibrationScreen(
            state = CalibrationUiState(
                isRunning = false,
                isCompleted = false,
                sampleCount = 0,
                targetSampleCount = 20
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrivacyScreenPreview() {
    ScrollJourneyTheme {
        PrivacyScreen(state = PrivacyUiState())
    }
}
