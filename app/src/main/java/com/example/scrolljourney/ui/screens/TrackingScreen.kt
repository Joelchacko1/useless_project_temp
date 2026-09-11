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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrolljourney.domain.ui.TrackingUiState
import com.example.scrolljourney.ui.components.NeoBadge
import com.example.scrolljourney.ui.components.NeoButton
import com.example.scrolljourney.ui.components.NeoInsetBlock
import com.example.scrolljourney.ui.components.NeoPanel
import com.example.scrolljourney.ui.components.NeoTopBar
import com.example.scrolljourney.ui.theme.LocalScrollJourneyColors
import com.example.scrolljourney.ui.theme.ScrollJourneyTheme

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
        NeoTopBar(
            title = "Tracking Control",
            fillColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
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
                TrackingToggleCard(
                    isEnabled = state.isTrackingEnabled,
                    serviceConnected = state.serviceConnected,
                    permissionGranted = state.isAccessibilityPermissionGranted,
                    onToggle = onToggleTracking
                )
            }

            item {
                TrackingStatusCard(state)
            }

            if (!state.isAccessibilityPermissionGranted) {
                item {
                    PermissionRequiredCard(onOpenAccessibilitySettings)
                }
            }

            item {
                CollectionInfoCard()
            }

            item {
                InformationCard()
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
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
    val extra = LocalScrollJourneyColors.current
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Scroll Tracking",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            NeoInsetBlock(
                fillColor = extra.reward,
                contentColor = extra.onReward,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NeoBadge(
                        text = if (isEnabled) "ON" else "OFF",
                        fillColor = if (isEnabled) extra.success else extra.borderInk,
                        contentColor = if (isEnabled) extra.onSuccess else MaterialTheme.colorScheme.background,
                    )

                    Spacer(modifier = Modifier.padding(12.dp))

                    Switch(
                        checked = isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = extra.success,
                            checkedTrackColor = MaterialTheme.colorScheme.background,
                            checkedBorderColor = MaterialTheme.colorScheme.outline,
                            uncheckedThumbColor = extra.borderInk,
                            uncheckedTrackColor = MaterialTheme.colorScheme.background,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                        )
                    )
                }
            }

            if (permissionGranted && serviceConnected) {
                Spacer(modifier = Modifier.height(16.dp))
                NeoInsetBlock(
                    fillColor = extra.success,
                    contentColor = extra.onSuccess,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = extra.onSuccess,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Service Connected",
                            fontSize = 14.sp,
                            color = extra.onSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
    val extra = LocalScrollJourneyColors.current
    NeoPanel(
        fillColor = extra.reward,
        contentColor = extra.onReward,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Status",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = extra.onReward,
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
                color = extra.onReward.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    isGranted: Boolean,
    modifier: Modifier = Modifier
) {
    val extra = LocalScrollJourneyColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = extra.onReward
        )

        NeoBadge(
            text = if (isGranted) "✓" else "✗",
            fillColor = if (isGranted) extra.success else MaterialTheme.colorScheme.error,
            contentColor = if (isGranted) extra.onSuccess else MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
private fun PermissionRequiredCard(
    onOpenAccessibilitySettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Permission Required",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "ScrollJourney needs Accessibility permission to detect scrolls. This permission is used ONLY to observe scroll events and is never used to capture screen content or typed text.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onErrorContainer,
            lineHeight = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        NeoButton(
            onClick = onOpenAccessibilitySettings,
            fillColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            modifier = Modifier.fillMaxWidth()
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

@Composable
private fun CollectionInfoCard(modifier: Modifier = Modifier) {
    val extra = LocalScrollJourneyColors.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NeoPanel(
            fillColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "What We Collect",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary,
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
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = item,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }

        NeoPanel(
            fillColor = extra.reward,
            contentColor = extra.onReward,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "What We DON'T Collect",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = extra.onReward,
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
                        color = extra.onReward,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = item,
                        fontSize = 12.sp,
                        color = extra.onReward
                    )
                }
            }
        }
    }
}

@Composable
private fun InformationCard(modifier: Modifier = Modifier) {
    NeoPanel(
        fillColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "How It Works",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            text = "When tracking is enabled, ScrollJourney listens for scroll events on your Android device through the accessibility API. All data is stored locally on your device — nothing is sent to any server.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            lineHeight = 18.sp
        )
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
