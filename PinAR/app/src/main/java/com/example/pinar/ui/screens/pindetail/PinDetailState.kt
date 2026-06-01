package com.example.pinar.ui.screens.pindetail

import com.example.pinar.data.CloudAnchorPin
import com.example.pinar.data.Comentario

data class PinDetailState(
    val isLoading: Boolean = true,
    val pin: CloudAnchorPin? = null,
    val comentarios: List<Comentario> = emptyList(),
    val error: String? = null,
    val isSendingComment: Boolean = false,
    val userLiked: Boolean = false,
    val uid: String? = "",
    val nombreCreador: String? = "",
    val fotoUrlCreador: String? = ""
)
