package com.example.scrolljourney.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrolljourney.domain.ui.TrackingUiState
import com.example.scrolljourney.ui.theme.ScrollJourneyTheme
import com.example.scrolljourney.ui.theme.SuccessGreen

@Composable
fun TrackingScreen(
    state: TrackingUiState,
    onToggleTracking: (Boolean) -> Unit = {},
    onOpenAccessibilitySettings: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top app bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Tracking Control",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Box(modifier = Modifier.size(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Main toggle
            item {
                TrackingToggleCard(
                    isEnabled = state.isTrackingEnabled,
                    serviceConnected = state.serviceConnected,
                    permissionGranted = state.isAccessibilityPermissionGranted,
                    onToggle = onToggleTracking
                )
            }

            // Status display
            item {
                TrackingStatusCard(state)
            }

            // Permission/settings call-to-action
            if (!state.isAccessibilityPermissionGranted) {
                item {
                    PermissionRequiredCard(onOpenAccessibilitySettings)
                }
            }

            // What is collected
            item {
                CollectionInfoCard()
            }

            // FAQ-style info
            item {
                InformationCard()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TrackingToggleCard(
    isEnabled: Boolean,
    serviceConnected: Boolean,
    permissionGranted: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Scroll Tracking",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEnabled) "ON" else "OFF",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) SuccessGreen else Color.Gray
                )

                Spacer(modifier = Modifier.padding(16.dp))

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.size(60.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SuccessGreen,
                        uncheckedThumbColor = Color.Gray
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (permissionGranted && serviceConnected) {
                Row(
                    modifier = Modifier
                        .background(
                            color = SuccessGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Service Connected",
                        fontSize = 14.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackingStatusCard(
    state: TrackingUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Status",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            StatusRow(
                label = "Accessibility Permission",
                isGranted = state.isAccessibilityPermissionGranted
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatusRow(
                label = "Service Connected",
                isGranted = state.serviceConnected
            )

            if (state.activeSinceEpochMs != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Active since app start",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    alpha = 0.6f
                )
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    isGranted: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isGranted) "✓" else "✗",
                fontSize = 16.sp,
                color = if (isGranted) SuccessGreen else Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PermissionRequiredCard(
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Permission Required",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "ScrollJourney needs Accessibility permission to detect scrolls. This permission is used ONLY to observe scroll events and is never used to capture screen content or typed text.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Button(
                onClick = onOpenAccessibilitySettings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp)
                )
                Text("Open Accessibility Settings")
            }
        }
    }
}

@Composable
private fun CollectionInfoCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "What We Collect",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            listOf(
                "Event timestamp",
                "Source app package identifier",
                "Scroll direction and magnitude"
            ).forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = item,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "What We DON'T Collect",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            listOf(
                "Screenshots or screen content",
                "Typed text or passwords",
                "Messages or personal data"
            ).forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓",
                        fontSize = 14.sp,
                        color = SuccessGreen,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = item,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun InformationCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "How It Works",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "When tracking is enabled, ScrollJourney listens for scroll events on your Android device through the accessibility API. All data is stored locally on your device — nothing is sent to any server.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                alpha = 0.8f,
                lineHeight = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackingScreenPreview() {
    ScrollJourneyTheme {
        TrackingScreen(
            state = TrackingUiState(
                isTrackingEnabled = true,
                serviceConnected = true,
                isAccessibilityPermissionGranted = true
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TrackingScreenNeedPermissionPreview() {
    ScrollJourneyTheme {
        TrackingScreen(
            state = TrackingUiState(
                isTrackingEnabled = false,
                serviceConnected = false,
                isAccessibilityPermissionGranted = false
            )
        )
    }
}
