package com.example.scrolljourney.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Neo-Brutalist Color Palette
val NeoCream = Color(0xFFF7F3EA)
val NeoYellow = Color(0xFFFFD928)
val NeoPurple = Color(0xFFA855F7)
val NeoPink = Color(0xFFFF3B8D)
val NeoGreen = Color(0xFF36E58D)
val NeoBlue = Color(0xFF5B8DEF)
val NeoOrange = Color(0xFFFF914D)
val NeoBlack = Color(0xFF111111)
val NeoWhite = Color(0xFFFFFFFF)

// Legacy palette aliases to maintain compatibility across existing screens if referenced
val PaletteCoral = NeoOrange
val PaletteIndigo = NeoBlack
val PaletteSkyBlue = NeoBlue
val PaletteGold = NeoYellow
val PaletteWhite = NeoWhite
val PaletteCharcoal = NeoBlack
val PalettePink = NeoPink
val PaletteSpringGreen = NeoGreen
val PaletteViolet = NeoPurple

// Custom roles Material3's ColorScheme has no slot for.
data class ScrollJourneyExtraColors(
    val cream: Color = NeoCream,
    val yellow: Color = NeoYellow,
    val purple: Color = NeoPurple,
    val pink: Color = NeoPink,
    val green: Color = NeoGreen,
    val blue: Color = NeoBlue,
    val orange: Color = NeoOrange,
    val black: Color = NeoBlack,
    val reward: Color = NeoYellow,
    val onReward: Color = NeoBlack,
    val success: Color = NeoGreen,
    val onSuccess: Color = NeoBlack,
    val borderInk: Color = NeoBlack,
)

val LocalScrollJourneyColors = staticCompositionLocalOf {
    ScrollJourneyExtraColors()
}

