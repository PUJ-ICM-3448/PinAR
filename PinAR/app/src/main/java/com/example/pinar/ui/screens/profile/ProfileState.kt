package com.example.pinar.ui.screens.profile

import com.example.pinar.data.CloudAnchorPin

data class ProfileState(
    val isLoading: Boolean = false,
    val pinesCreados: Int = 0,
    val rutasNavegadas: Int = 0,
    val logros: Int = 0,
    val error: String? = null,
    val lista: List<CloudAnchorPin> = emptyList(),
    val uid: String = "",
    val comentarios: Int = 0
)
