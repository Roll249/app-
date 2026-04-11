package com.fintech.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// =============================================================================
// EDITORIAL FINANCE COLOR SCHEMES
// =============================================================================

private val EditorialLightColorScheme = lightColorScheme(
    // Primary - Deep Navy
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    
    // Secondary - Emerald
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    
    // Tertiary
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    
    // Error
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    
    // Surface
    background = Surface,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceBright = SurfaceBright,
    surfaceDim = SurfaceDim,
    surfaceContainer = SurfaceContainer,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceTint = SurfaceTint,
    surfaceVariant = SurfaceContainerLow,
    onSurfaceVariant = OnSurfaceVariant,
    
    // Inverse
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary,
    
    // Outline
    outline = Outline,
    outlineVariant = OutlineVariant
)

private val EditorialDarkColorScheme = darkColorScheme(
    // Primary
    primary = OnPrimary,
    onPrimary = Primary,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = PrimaryLight,
    
    // Secondary
    secondary = SecondaryLight,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryDark,
    onSecondaryContainer = SecondaryLight,
    
    // Tertiary
    tertiary = TertiaryFixed,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryDark,
    onTertiaryContainer = TertiaryFixed,
    
    // Error
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    
    // Surface
    background = SurfaceDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceBright = SurfaceBrightDark,
    surfaceDim = SurfaceDim,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceTint = SurfaceTint,
    surfaceVariant = SurfaceContainerLowDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    
    // Inverse
    inverseSurface = SurfaceContainerLowest,
    inverseOnSurface = OnSurface,
    inversePrimary = PrimaryLight,
    
    // Outline
    outline = Outline,
    outlineVariant = OutlineVariant
)

// Legacy compatibility
private val DarkColorScheme = EditorialDarkColorScheme
private val LightColorScheme = EditorialLightColorScheme

// =============================================================================
// THEME COMPOSABLE
// =============================================================================

@Composable
fun FintechTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> EditorialDarkColorScheme
        else -> EditorialLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EditorialTypography,
        content = content
    )
}
