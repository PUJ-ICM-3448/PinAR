package com.example.pinar.data.mock

import com.example.pinar.data.models.Notification
import com.example.pinar.data.models.NotificationType

val mockNotifications = listOf(
    Notification(
        id = "1",
        title = "Nuevo comentario",
        detail = "Ana Torres comentó en tu pin",
        preview = "Cafetería Central",
        time = "Hace 5 min",
        unread = true,
        type = NotificationType.COMMENTS
    ),
    Notification(
        id = "2",
        title = "Le gustó tu pin",
        detail = "Carlos Ruiz le dio me gusta a tu publicación",
        preview = "Biblioteca Principal",
        time = "Hace 15 min",
        unread = true,
        type = NotificationType.LIKES
    ),
    Notification(
        id = "3",
        title = "Te mencionaron",
        detail = "Luis Mendoza te mencionó en un comentario",
        preview = "Laboratorio 5",
        time = "Hace 1 hora",
        unread = true,
        type = NotificationType.COMMENTS
    ),
    Notification(
        id = "4",
        title = "Nuevo seguidor",
        detail = "María González comenzó a seguirte",
        preview = null,
        time = "Ayer",
        unread = false,
        type = NotificationType.FOLLOWERS
    ),
    Notification(
        id = "5",
        title = "Respuesta a tu comentario",
        detail = "Diego Vargas respondió a tu comentario",
        preview = "Auditorio Mayor",
        time = "Ayer",
        unread = false,
        type = NotificationType.COMMENTS
    ),
    Notification(
        id = "6",
        title = "Múltiples me gusta",
        detail = "5 personas le dieron me gusta a tu pin",
        preview = "Gimnasio",
        time = "Hace 2 días",
        unread = false,
        type = NotificationType.LIKES
    ),
    Notification(
        id = "7",
        title = "Reporte revisado",
        detail = "Tu reporte ha sido revisado por los moderadores",
        preview = null,
        time = "Hace 3 días",
        unread = false,
        type = NotificationType.SYSTEM
    )
)
