package com.example.pinar.ui.screens.notifications

import com.example.pinar.data.models.Notification
import com.example.pinar.data.models.NotificationType

data class NotificationsState(
    val notificaciones: List<Notification> = emptyList(),
    val todasLeidas: Boolean = false,
    val filtroSeleccionado: NotificationType = NotificationType.ALL,
    val filterChipsVisible: Boolean = false
)
