package com.example.scrolljourney.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightScrollJourneyColorScheme = lightColorScheme(
    primary = PaletteIndigo,
    onPrimary = PaletteWhite,
    primaryContainer = PaletteIndigo,
    onPrimaryContainer = PaletteWhite,
    secondary = PaletteSkyBlue,
    onSecondary = PaletteWhite,
    secondaryContainer = PaletteSkyBlue,
    onSecondaryContainer = PaletteWhite,
    tertiary = PaletteCoral,
    onTertiary = PaletteIndigo,
    tertiaryContainer = PaletteCoral,
    onTertiaryContainer = PaletteIndigo,
    error = PaletteCoral,
    onError = PaletteIndigo,
    errorContainer = PaletteCoral,
    onErrorContainer = PaletteIndigo,
    background = PaletteWhite,
    onBackground = PaletteIndigo,
    surface = PaletteWhite,
    onSurface = PaletteIndigo,
    surfaceVariant = PaletteGold,
    onSurfaceVariant = PaletteIndigo,
    surfaceTint = PaletteIndigo,
    surfaceContainerLowest = PaletteWhite,
    surfaceContainerLow = PaletteWhite,
    surfaceContainer = PaletteWhite,
    surfaceContainerHigh = PaletteWhite,
    surfaceContainerHighest = PaletteWhite,
    surfaceDim = PaletteWhite,
    surfaceBright = PaletteWhite,
    outline = PaletteCharcoal,
    outlineVariant = PaletteCharcoal,
    scrim = PaletteCharcoal,
    inverseSurface = PaletteCharcoal,
    inverseOnSurface = PaletteWhite,
    inversePrimary = PaletteViolet,
)

private val DarkScrollJourneyColorScheme = darkColorScheme(
    primary = PaletteViolet,
    onPrimary = PaletteWhite,
    primaryContainer = PaletteViolet,
    onPrimaryContainer = PaletteWhite,
    secondary = PaletteSpringGreen,
    onSecondary = PaletteCharcoal,
    secondaryContainer = PaletteSpringGreen,
    onSecondaryContainer = PaletteCharcoal,
    tertiary = PalettePink,
    onTertiary = PaletteWhite,
    tertiaryContainer = PalettePink,
    onTertiaryContainer = PaletteWhite,
    error = PalettePink,
    onError = PaletteWhite,
    errorContainer = PalettePink,
    onErrorContainer = PaletteWhite,
    background = PaletteCharcoal,
    onBackground = PaletteWhite,
    surface = PaletteCharcoal,
    onSurface = PaletteWhite,
    surfaceVariant = PaletteGold,
    onSurfaceVariant = PaletteCharcoal,
    surfaceTint = PaletteViolet,
    surfaceContainerLowest = PaletteCharcoal,
    surfaceContainerLow = PaletteCharcoal,
    surfaceContainer = PaletteCharcoal,
    surfaceContainerHigh = PaletteCharcoal,
    surfaceContainerHighest = PaletteCharcoal,
    surfaceDim = PaletteCharcoal,
    surfaceBright = PaletteCharcoal,
    outline = PaletteWhite,
    outlineVariant = PaletteWhite,
    scrim = PaletteCharcoal,
    inverseSurface = PaletteWhite,
    inverseOnSurface = PaletteCharcoal,
    inversePrimary = PaletteIndigo,
)

private val LightExtraColors = ScrollJourneyExtraColors(
    reward = PaletteGold,
    onReward = PaletteIndigo,
    success = PaletteSkyBlue,
    onSuccess = PaletteWhite,
    borderInk = PaletteCharcoal,
)

private val DarkExtraColors = ScrollJourneyExtraColors(
    reward = PaletteGold,
    onReward = PaletteCharcoal,
    success = PaletteSpringGreen,
    onSuccess = PaletteCharcoal,
    borderInk = PaletteWhite,
)

@Composable
fun ScrollJourneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkScrollJourneyColorScheme else LightScrollJourneyColorScheme
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    CompositionLocalProvider(LocalScrollJourneyColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
