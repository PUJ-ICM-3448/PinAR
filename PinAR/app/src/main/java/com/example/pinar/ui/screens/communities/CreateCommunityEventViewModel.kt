package com.example.pinar.ui.screens.communities

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.data.CommunityEvent
import com.example.pinar.data.CommunityEventRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

data class CreateCommunityEventState(
    val name: String = "",
    val description: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

class CreateCommunityEventViewModel(
    private val eventRepository: CommunityEventRepository = CommunityEventRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _state = mutableStateOf(CreateCommunityEventState())
    val state: State<CreateCommunityEventState> = _state

    fun updateName(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun updateDescription(value: String) {
        _state.value = _state.value.copy(description = value)
    }

    fun createEvent(communityId: String, onSuccess: (String) -> Unit) {
        val name = _state.value.name.trim()
        if (name.isBlank()) {
            _state.value = _state.value.copy(error = "El nombre es obligatorio")
            return
        }
        val uid = auth.currentUser?.uid.orEmpty()
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            runCatching {
                eventRepository.createEvent(
                    communityId,
                    CommunityEvent(
                        name = name,
                        description = _state.value.description.trim(),
                        createdBy = uid,
                        participants = listOf(uid)
                    )
                )
            }.onSuccess { eventId ->
                _state.value = _state.value.copy(isSaving = false)
                onSuccess(eventId)
            }.onFailure {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = it.message ?: "No se pudo crear el evento"
                )
            }
        }
    }
}
