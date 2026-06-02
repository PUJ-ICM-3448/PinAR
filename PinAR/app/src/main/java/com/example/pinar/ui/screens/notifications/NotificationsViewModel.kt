package com.example.pinar.ui.screens.notifications

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.pinar.data.models.Notification
import com.example.pinar.data.models.NotificationType

class NotificationsViewModel : ViewModel() {
    private val _state = mutableStateOf(NotificationsState(notificaciones = emptyList()))
    val state: State<NotificationsState> = _state

    fun toggleFilterChips() {
        _state.value = _state.value.copy(filterChipsVisible = !_state.value.filterChipsVisible)
    }

    fun setFiltro(tipo: NotificationType) {
        _state.value = _state.value.copy(filtroSeleccionado = tipo)
    }

    fun marcarTodasLeidas() {
        _state.value = _state.value.copy(todasLeidas = true)
    }

    fun borrarNotificaciones() {
        _state.value = _state.value.copy(notificaciones = emptyList())
    }

    fun removeNotification(notif: Notification) {
        _state.value = _state.value.copy(notificaciones = _state.value.notificaciones.filter { it != notif })
    }
}
