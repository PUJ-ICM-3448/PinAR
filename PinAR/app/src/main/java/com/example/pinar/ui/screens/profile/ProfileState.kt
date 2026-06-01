package com.example.pinar.ui.screens.profile

import com.example.pinar.data.CloudAnchorPin

data class ProfileState(
    val lista: List<CloudAnchorPin> = emptyList(),
    val uid: String = "",
    val comentarios: Int = 0
)
