package com.example.scrolljourney.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Energetic Neobrutalism Minimalist Palette — the only 10 hex values used anywhere in the UI.

// Light group
val PaletteCoral = Color(0xFFFF6F61)
val PaletteIndigo = Color(0xFF4B0082)
val PaletteSkyBlue = Color(0xFF00BFFF)
val PaletteGold = Color(0xFFFFD700) // shared reward color, both themes
val PaletteWhite = Color(0xFFFFFFFF)

// Dark group
val PaletteCharcoal = Color(0xFF333333)
val PalettePink = Color(0xFFFF4081)
val PaletteSpringGreen = Color(0xFF00FF7F)
val PaletteViolet = Color(0xFF8A2BE2)
// PaletteGold reused here (shared reward color)

// Custom roles Material3's ColorScheme has no slot for.
data class ScrollJourneyExtraColors(
    val reward: Color,
    val onReward: Color,
    val success: Color,
    val onSuccess: Color,
    val borderInk: Color,
)

val LocalScrollJourneyColors = staticCompositionLocalOf {
    ScrollJourneyExtraColors(
        reward = PaletteGold,
        onReward = PaletteIndigo,
        success = PaletteSkyBlue,
        onSuccess = PaletteWhite,
        borderInk = PaletteCharcoal,
    )
}
