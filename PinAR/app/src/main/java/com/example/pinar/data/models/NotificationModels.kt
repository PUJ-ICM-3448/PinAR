package com.example.pinar.data.models

enum class NotificationType {
    ALL,
    COMMENTS,
    LIKES,
    FOLLOWERS,
    COMMUNITIES,
    SYSTEM
}

data class Notification(
    val id: String = "",
    val title: String = "",
    val detail: String = "",
    val preview: String? = null,
    val time: String = "",
    val unread: Boolean = false,
    val type: NotificationType = NotificationType.ALL
)
