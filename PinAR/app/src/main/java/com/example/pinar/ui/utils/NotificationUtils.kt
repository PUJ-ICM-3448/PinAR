package com.example.pinar.ui.utils

import androidx.compose.ui.graphics.Color
import com.example.pinar.ui.theme.MutedGold
import com.example.pinar.ui.theme.SoftCream

val avatarColors = listOf(
    Color(0xFFE3F2FD), // azul claro
    Color(0xFFFCE4EC), // rosa claro
    Color(0xFFE8F5E9), // verde claro
    Color(0xFFFFF3E0), // naranja claro
    Color(0xFFF3E5F5), // violeta claro
    Color(0xFFE0F7FA), // cyan claro
    Color(0xFFFFEBEE), // rojo muy claro
    Color(0xFFEFEBE9), // marrón claro
    MutedGold.copy(alpha = 0.3f),
    SoftCream
)

fun avatarColorFor(name: String): Color =
    avatarColors[kotlin.math.abs(name.hashCode()) % avatarColors.size]

fun initialFromDetalle(detalle: String): String {
    val firstWord = detalle.trim().split(Regex("\\s+")).firstOrNull() ?: return "?"
    return firstWord.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}
