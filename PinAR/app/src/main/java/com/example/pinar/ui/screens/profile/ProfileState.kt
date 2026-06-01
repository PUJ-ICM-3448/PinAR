package com.example.pinar.ui.screens.profile

data class ProfileState(
    val isLoading: Boolean = false,
    val pinesCreados: Int = 0,
    val rutasNavegadas: Int = 0,
    val logros: Int = 0,
    val error: String? = null
)
