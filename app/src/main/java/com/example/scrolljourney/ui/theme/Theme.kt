package com.example.scrolljourney.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightScrollJourneyColorScheme = lightColorScheme(
    primary = NeoPurple,
    onPrimary = NeoWhite,
    primaryContainer = NeoPurple,
    onPrimaryContainer = NeoWhite,
    secondary = NeoYellow,
    onSecondary = NeoBlack,
    secondaryContainer = NeoYellow,
    onSecondaryContainer = NeoBlack,
    tertiary = NeoPink,
    onTertiary = NeoWhite,
    tertiaryContainer = NeoPink,
    onTertiaryContainer = NeoWhite,
    error = NeoPink,
    onError = NeoWhite,
    errorContainer = NeoPink,
    onErrorContainer = NeoWhite,
    background = NeoCream,
    onBackground = NeoBlack,
    surface = NeoCream,
    onSurface = NeoBlack,
    surfaceVariant = NeoYellow,
    onSurfaceVariant = NeoBlack,
    surfaceTint = NeoPurple,
    surfaceContainerLowest = NeoCream,
    surfaceContainerLow = NeoCream,
    surfaceContainer = NeoCream,
    surfaceContainerHigh = NeoCream,
    surfaceContainerHighest = NeoCream,
    surfaceDim = NeoCream,
    surfaceBright = NeoCream,
    outline = NeoBlack,
    outlineVariant = NeoBlack,
    scrim = NeoBlack,
    inverseSurface = NeoBlack,
    inverseOnSurface = NeoWhite,
    inversePrimary = NeoPurple,
)

private val DarkScrollJourneyColorScheme = LightScrollJourneyColorScheme

private val LightExtraColors = ScrollJourneyExtraColors(
    cream = NeoCream,
    yellow = NeoYellow,
    purple = NeoPurple,
    pink = NeoPink,
    green = NeoGreen,
    blue = NeoBlue,
    orange = NeoOrange,
    black = NeoBlack,
    reward = NeoYellow,
    onReward = NeoBlack,
    success = NeoGreen,
    onSuccess = NeoBlack,
    borderInk = NeoBlack,
)

private val DarkExtraColors = LightExtraColors

@Composable
fun ScrollJourneyTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightScrollJourneyColorScheme
    val extraColors = LightExtraColors

    CompositionLocalProvider(LocalScrollJourneyColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
