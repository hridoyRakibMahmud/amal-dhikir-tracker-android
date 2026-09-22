package com.example.amaldhikirtracker.ui.theme

import androidx.compose.ui.graphics.Color

// Design tokens from the design handoff (design_handoff_amal_dhikir_tracker/README.md)

// Light theme
val BackgroundLight = Color(0xFFFAF5EA)
val SurfaceLight = Color(0xFFFFFFFF)
val Surface2Light = Color(0xFFF0E7D2)
val BorderLight = Color(0xFFE6DAC0)
val TextLight = Color(0xFF2A2A20)
val TextMutedLight = Color(0xFF7A7360)
val GreenLight = Color(0xFF2F5233)
val GreenStrongLight = Color(0xFF1E3A22)
val GreenTintLight = Color(0xFFE4EBDD)
val OnGreenLight = Color(0xFFFFFFFF)
val GoldLight = Color(0xFFC08A2E)
val GoldStrongLight = Color(0xFF8F6620)
val GoldTintLight = Color(0xFFF6EAD0)
val OnGoldLight = Color(0xFF2A2010)
val DangerLight = Color(0xFFB3492F)
val DangerTintLight = Color(0xFFF5E1D8)

// Dark theme
val BackgroundDark = Color(0xFF141C17)
val SurfaceDark = Color(0xFF1D2820)
val Surface2Dark = Color(0xFF243027)
val BorderDark = Color(0xFF334033)
val TextDark = Color(0xFFEEE8D8)
val TextMutedDark = Color(0xFFA79E88)
val GreenDark = Color(0xFF84C495)
val GreenStrongDark = Color(0xFFA9D9B6)
val GreenTintDark = Color(0xFF26372B)
val OnGreenDark = Color(0xFF12251A)
val GoldDark = Color(0xFFE3BA6E)
val GoldStrongDark = Color(0xFFF1CE8E)
val GoldTintDark = Color(0xFF3A2F19)
val OnGoldDark = Color(0xFF241B08)
val DangerDark = Color(0xFFE38A70)
val DangerTintDark = Color(0xFF3A241D)

/**
 * Full design-token set, mirroring the handoff's Light/Dark token objects exactly.
 * M3's ColorScheme roles are a lossy fit for these (no "gold" or "surface2" role), so
 * screens that need exact fidelity should read from [LocalAppColors] via `AppTheme.colors`
 * instead of squeezing everything through `MaterialTheme.colorScheme`.
 */
data class AppColors(
    val background: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val text: Color,
    val textMuted: Color,
    val green: Color,
    val greenStrong: Color,
    val greenTint: Color,
    val onGreen: Color,
    val gold: Color,
    val goldStrong: Color,
    val goldTint: Color,
    val onGold: Color,
    val danger: Color,
    val dangerTint: Color
)

val LightAppColors = AppColors(
    background = BackgroundLight,
    surface = SurfaceLight,
    surface2 = Surface2Light,
    border = BorderLight,
    text = TextLight,
    textMuted = TextMutedLight,
    green = GreenLight,
    greenStrong = GreenStrongLight,
    greenTint = GreenTintLight,
    onGreen = OnGreenLight,
    gold = GoldLight,
    goldStrong = GoldStrongLight,
    goldTint = GoldTintLight,
    onGold = OnGoldLight,
    danger = DangerLight,
    dangerTint = DangerTintLight
)

val DarkAppColors = AppColors(
    background = BackgroundDark,
    surface = SurfaceDark,
    surface2 = Surface2Dark,
    border = BorderDark,
    text = TextDark,
    textMuted = TextMutedDark,
    green = GreenDark,
    greenStrong = GreenStrongDark,
    greenTint = GreenTintDark,
    onGreen = OnGreenDark,
    gold = GoldDark,
    goldStrong = GoldStrongDark,
    goldTint = GoldTintDark,
    onGold = OnGoldDark,
    danger = DangerDark,
    dangerTint = DangerTintDark
)
