package com.example.amaldhikirtracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GreenDark,
    onPrimary = OnGreenDark,
    primaryContainer = GreenTintDark,
    onPrimaryContainer = GreenDark,
    secondary = GoldDark,
    onSecondary = OnGoldDark,
    secondaryContainer = GoldTintDark,
    onSecondaryContainer = GoldDark,
    tertiary = GoldDark,
    onTertiary = OnGoldDark,
    tertiaryContainer = GoldTintDark,
    onTertiaryContainer = GoldDark,
    background = BackgroundDark,
    onBackground = TextDark,
    surface = SurfaceDark,
    onSurface = TextDark,
    surfaceVariant = Surface2Dark,
    onSurfaceVariant = TextMutedDark,
    outline = BorderDark,
    outlineVariant = BorderDark,
    error = DangerDark,
    errorContainer = DangerTintDark,
    onError = BackgroundDark,
    onErrorContainer = DangerDark
)

private val LightColorScheme = lightColorScheme(
    primary = GreenLight,
    onPrimary = OnGreenLight,
    primaryContainer = GreenTintLight,
    onPrimaryContainer = GreenLight,
    secondary = GoldLight,
    onSecondary = OnGoldLight,
    secondaryContainer = GoldTintLight,
    onSecondaryContainer = GoldLight,
    tertiary = GoldLight,
    onTertiary = OnGoldLight,
    tertiaryContainer = GoldTintLight,
    onTertiaryContainer = GoldLight,
    background = BackgroundLight,
    onBackground = TextLight,
    surface = SurfaceLight,
    onSurface = TextLight,
    surfaceVariant = Surface2Light,
    onSurfaceVariant = TextMutedLight,
    outline = BorderLight,
    outlineVariant = BorderLight,
    error = DangerLight,
    errorContainer = DangerTintLight,
    onError = BackgroundLight,
    onErrorContainer = DangerLight
)

/** Exact design tokens (Green/Gold/Surface2/etc.) that don't map 1:1 onto M3 ColorScheme roles. */
val LocalAppColors = compositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}

@Composable
fun AmalDhikirTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
