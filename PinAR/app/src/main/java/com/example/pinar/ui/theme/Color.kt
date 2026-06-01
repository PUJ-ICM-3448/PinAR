package com.example.pinar.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────
// Rojo — solo donde importa
// ─────────────────────────────────────────
val RedPrimary        = Color(0xFFC41C1C)  // botones, pines activos, nav activo
val RedBright         = Color(0xFFE53935)  // hover / pressed
val RedAccent         = Color(0xFFFF5252)  // badges, highlights puntuales
val RedContainerDark  = Color(0xFF1F0A0A)  // fondo de chips/cards en dark
val RedContainerLight = Color(0xFFFDEAEA)  // fondo de chips/cards en light
val OnRedContainerD   = Color(0xFFFF5252)  // texto sobre RedContainerDark
val OnRedContainerL   = Color(0xFFC41C1C)  // texto sobre RedContainerLight

// ─────────────────────────────────────────
// Slate oscuro
// ─────────────────────────────────────────
val SlateBackground   = Color(0xFF0E1117)  // fondo dark
val SlateSurface      = Color(0xFF161B25)  // cards, sheets en dark
val SlateSurfaceVar   = Color(0xFF1E2433)  // superficies elevadas en dark
val SlateOutline      = Color(0xFF2A3040)  // bordes en dark
val SlateOutlineVar   = Color(0xFF1E2433)  // bordes sutiles en dark

// ─────────────────────────────────────────
// Slate claro — el "negativo" del slate oscuro
// Misma familia fría azul-gris, invertida
// ─────────────────────────────────────────
val LightBackground   = Color(0xFFF0F3F9)  // fondo: blanco con tinte slate frío
val LightSurface      = Color(0xFFFFFFFF)  // cards, sheets
val LightSurfaceVar   = Color(0xFFE4E9F2)  // superficies elevadas / inputs
val LightOutline      = Color(0xFFC8D0DE)  // bordes visibles
val LightOutlineVar   = Color(0xFFE4E9F2)  // bordes sutiles

// ─────────────────────────────────────────
// Texto dark
// ─────────────────────────────────────────
val TextPrimaryDark    = Color(0xFFE8EDF2)  // texto principal
val TextSecondaryDark  = Color(0xFF78909C)  // texto secundario / hints

// ─────────────────────────────────────────
// Texto light — mismo slate, modo claro
// ─────────────────────────────────────────
val TextPrimaryLight   = Color(0xFF0E1117)  // mismo que SlateBackground invertido
val TextSecondaryLight = Color(0xFF5A6473)  // gris frío, coherente con la paleta

// ─────────────────────────────────────────
// Error — rojo en ambos modos
// ─────────────────────────────────────────
val ErrorDark          = Color(0xFFFF5252)
val ErrorLight         = Color(0xFFC41C1C)
val OnError            = Color(0xFFFFFFFF)
val ErrorContainerDark = Color(0xFF1F0A0A)
val OnErrorContainerD  = Color(0xFFFF5252)
val ErrorContainerLight= Color(0xFFFDEAEA)
val OnErrorContainerL  = Color(0xFFC41C1C)

// Mantenemos estas por compatibilidad con el resto del proyecto si es necesario
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

// Aliases para evitar errores de compilación en otras partes del código que usen nombres antiguos
val OnRedContainer = OnRedContainerD
val ErrorColor = ErrorDark
val OnErrorColor = OnError
val ErrorContainer = ErrorContainerDark
val OnErrorContainer = OnErrorContainerD
