package com.example.pinar.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─────────────────────────────────────────
// Dark scheme — Slate profundo + rojo señal
// ─────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = RedPrimary,
    onPrimary            = Color.White,
    primaryContainer     = RedContainerDark,
    onPrimaryContainer   = OnRedContainer,

    secondary            = TextSecondaryDark,
    onSecondary          = SlateBackground,
    secondaryContainer   = SlateSurfaceVar,
    onSecondaryContainer = TextPrimaryDark,

    tertiary             = RedAccent,
    onTertiary           = Color.White,
    tertiaryContainer    = RedContainerDark,
    onTertiaryContainer  = RedAccent,

    background           = SlateBackground,
    onBackground         = TextPrimaryDark,

    surface              = SlateSurface,
    onSurface            = TextPrimaryDark,
    surfaceVariant       = SlateSurfaceVar,
    onSurfaceVariant     = TextSecondaryDark,

    outline              = SlateOutline,
    outlineVariant       = SlateSurfaceVar,

    error                = ErrorColor,
    onError              = OnErrorColor,
    errorContainer       = ErrorContainer,
    onErrorContainer     = OnErrorContainer,

    inverseSurface       = LightSurface,
    inverseOnSurface     = TextPrimaryLight,
    inversePrimary       = RedBright,
)

// ─────────────────────────────────────────
// Light scheme — limpio con rojo preciso
// ─────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary              = RedPrimary,
    onPrimary            = Color.White,
    primaryContainer     = RedContainerLight,
    onPrimaryContainer   = OnRedContainerL,

    secondary            = TextSecondaryLight,
    onSecondary          = Color.White,
    secondaryContainer   = LightSurfaceVar,
    onSecondaryContainer = TextPrimaryLight,

    tertiary             = RedBright,
    onTertiary           = Color.White,
    tertiaryContainer    = RedContainerLight,
    onTertiaryContainer  = RedPrimary,

    background           = LightBackground,
    onBackground         = TextPrimaryLight,

    surface              = LightSurface,
    onSurface            = TextPrimaryLight,
    surfaceVariant       = LightSurfaceVar,
    onSurfaceVariant     = TextSecondaryLight,

    outline              = LightOutline,
    outlineVariant       = LightSurfaceVar,

    error                = RedPrimary,
    onError              = Color.White,
    errorContainer       = RedContainerLight,
    onErrorContainer     = OnRedContainerL,

    inverseSurface       = SlateSurface,
    inverseOnSurface     = TextPrimaryDark,
    inversePrimary       = RedAccent,
)

// ─────────────────────────────────────────
// Tema principal
// ─────────────────────────────────────────
@Composable
fun PinARTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Colorea la status bar acorde al tema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
