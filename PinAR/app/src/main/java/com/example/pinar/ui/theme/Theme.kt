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
// Dark — Slate profundo + rojo señal
// ─────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary              = RedPrimary,
    onPrimary            = Color.White,
    primaryContainer     = RedContainerDark,
    onPrimaryContainer   = OnRedContainerD,

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
    outlineVariant       = SlateOutlineVar,

    error                = ErrorDark,
    onError              = OnError,
    errorContainer       = ErrorContainerDark,
    onErrorContainer     = OnErrorContainerD,

    inverseSurface       = LightSurface,
    inverseOnSurface     = TextPrimaryLight,
    inversePrimary       = RedBright,
)

// ─────────────────────────────────────────
// Light — Slate invertido + rojo señal
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
    onTertiaryContainer  = OnRedContainerL,

    background           = LightBackground,
    onBackground         = TextPrimaryLight,

    surface              = LightSurface,
    onSurface            = TextPrimaryLight,
    surfaceVariant       = LightSurfaceVar,
    onSurfaceVariant     = TextSecondaryLight,

    outline              = LightOutline,
    outlineVariant       = LightOutlineVar,

    error                = ErrorLight,
    onError              = OnError,
    errorContainer       = ErrorContainerLight,
    onErrorContainer     = OnErrorContainerL,

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
