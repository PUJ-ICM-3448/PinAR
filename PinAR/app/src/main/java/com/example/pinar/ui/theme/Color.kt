package com.example.pinar.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────
// Rojo — solo donde importa
// ─────────────────────────────────────────
val RedPrimary       = Color(0xFFC41C1C)  // botones, pines activos, nav activo
val RedBright        = Color(0xFFE53935)  // hover / pressed
val RedAccent        = Color(0xFFFF5252)  // badges, highlights puntuales
val RedContainerDark = Color(0xFF1F0A0A)  // fondo de chips/cards en dark
val RedContainerLight= Color(0xFFFDEAEA)  // fondo de chips/cards en light
val OnRedContainer   = Color(0xFFFF5252)  // texto sobre RedContainerDark
val OnRedContainerL  = Color(0xFFC41C1C)  // texto sobre RedContainerLight

// ─────────────────────────────────────────
// Slate — base oscura
// ─────────────────────────────────────────
val SlateBackground  = Color(0xFF0E1117)  // fondo dark
val SlateSurface     = Color(0xFF161B25)  // cards, sheets en dark
val SlateSurfaceVar  = Color(0xFF1E2433)  // superficies elevadas en dark
val SlateOutline     = Color(0xFF2A3040)  // bordes en dark

// ─────────────────────────────────────────
// Slate — base clara
// ─────────────────────────────────────────
val LightBackground  = Color(0xFFF4F6FA)
val LightSurface     = Color(0xFFFFFFFF)
val LightSurfaceVar  = Color(0xFFEAEEF5)
val LightOutline     = Color(0xFFD0D6E2)

// ─────────────────────────────────────────
// Texto
// ─────────────────────────────────────────
val TextPrimaryDark   = Color(0xFFE8EDF2)  // texto principal en dark
val TextSecondaryDark = Color(0xFF78909C)  // texto secundario en dark
val TextPrimaryLight  = Color(0xFF0E1117)  // texto principal en light
val TextSecondaryLight= Color(0xFF5A6473)  // texto secundario en light

// ─────────────────────────────────────────
// Error (usa rojo brillante en ambos modos)
// ─────────────────────────────────────────
val ErrorColor       = Color(0xFFFF5252)
val OnErrorColor     = Color(0xFFFFFFFF)
val ErrorContainer   = Color(0xFF1F0A0A)
val OnErrorContainer = Color(0xFFFF5252)

// Mantenemos estas si se usan en otros sitios o por compatibilidad inicial
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val RedDark = Color(0xFF8B0000)
val RedDeep = Color(0xFF4A0000)
val Charcoal = Color(0xFF2D2D2D)
val MutedGold = Color(0xFFC5A059)
val SoftCream = Color(0xFFF5F5F5)
