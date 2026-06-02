package com.example.pinar.ui.screens.communities

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.data.CloudAnchorRepository
import com.example.pinar.data.CommunityEventRepository
import com.example.pinar.data.CommunityRepository
import com.example.pinar.ui.MainViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class CommunityDetailViewModel(
    private val communityRepository: CommunityRepository = CommunityRepository(),
    private val pinRepository: CloudAnchorRepository = CloudAnchorRepository(),
    private val eventRepository: CommunityEventRepository = CommunityEventRepository()
) : ViewModel() {

    private val _state = mutableStateOf(CommunityDetailState())
    val state: State<CommunityDetailState> = _state
    private var eventsJob: Job? = null

    fun load(communityId: String, myCommunityIds: Set<String>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching {
                val community = communityRepository.getCommunity(communityId)
                val uid = communityRepository.currentUid()
                val isMember = community.members.contains(uid)
                        || communityId in myCommunityIds
                val pins = if (isMember || community.isPublic) {
                    try {
                        pinRepository.getPinsForCommunity(communityId)
                    } catch (e: Exception) {
                        Log.w("CommunityDetailViewModel", "No se pudieron cargar pines", e)
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                Triple(community, isMember, pins)
            }
                .onSuccess { (community, isMember, pins) ->
                    _state.value = CommunityDetailState(
                        community = community,
                        sharedPins = pins,
                        isLoading = false,
                        isMember = isMember
                    )
                    if (isMember) observeEvents(communityId)
                }
                .onFailure { error ->
                    Log.e("CommunityDetailViewModel", "Error cargando comunidad", error)
                    _state.value = CommunityDetailState(
                        isLoading = false,
                        error = toUserMessage(error)
                    )
                }
        }
    }

    private fun observeEvents(communityId: String) {
        eventsJob?.cancel()
        eventsJob = viewModelScope.launch {
            eventRepository.observeActiveEvents(communityId)
                .catch { Log.w("CommunityDetailViewModel", "Error eventos", it) }
                .collect { events ->
                    _state.value = _state.value.copy(activeEvents = events)
                }
        }
    }

    fun joinCommunity(communityId: String, mainViewModel: MainViewModel) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isJoinLeaveInProgress = true, actionMessage = null)
            runCatching { communityRepository.joinCommunity(communityId) }
                .onSuccess {
                    mainViewModel.refreshUserData()
                    val ids = mainViewModel.userData.value?.memberOf.orEmpty()
                        .map { it.id }.toSet() + communityId
                    load(communityId, ids)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isJoinLeaveInProgress = false,
                        actionMessage = toUserMessage(error)
                    )
                }
        }
    }

    fun leaveCommunity(communityId: String, mainViewModel: MainViewModel) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isJoinLeaveInProgress = true, actionMessage = null)
            runCatching { communityRepository.leaveCommunity(communityId) }
                .onSuccess {
                    eventsJob?.cancel()
                    mainViewModel.refreshUserData()
                    val ids = mainViewModel.userData.value?.memberOf.orEmpty()
                        .map { it.id }.toSet() - communityId
                    load(communityId, ids)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        isJoinLeaveInProgress = false,
                        actionMessage = toUserMessage(error)
                    )
                }
        }
    }

    fun updateCommunity(
        name: String,
        description: String,
        isPublic: Boolean,
        imageUri: Uri?,
        context: Context,
        mainViewModel: MainViewModel,
        onUpdated: () -> Unit
    ) {
        val current = _state.value.community ?: return
        if (name.isBlank()) {
            _state.value = _state.value.copy(actionMessage = "El nombre es obligatorio")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isUpdating = true, actionMessage = null)
            runCatching {
                var imageUrl = current.imageUrl
                if (imageUri != null) {
                    imageUrl = communityRepository.uploadCommunityImage(
                        current.id,
                        imageUri,
                        context
                    )
                }
                val updated = communityRepository.updateCommunity(
                    current.copy(
                        name = name.trim(),
                        description = description.trim(),
                        isPublic = isPublic,
                        imageUrl = imageUrl
                    )
                )
                mainViewModel.refreshUserData()
                val ids = mainViewModel.userData.value?.memberOf.orEmpty()
                    .map { it.id }.toSet()
                load(updated.id, ids)
                updated
            }
                .onSuccess {
                    _state.value = _state.value.copy(isUpdating = false)
                    onUpdated()
                }
                .onFailure { error ->
                    Log.e("CommunityDetailViewModel", "Error actualizando comunidad", error)
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        actionMessage = toUserMessage(error)
                    )
                }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        eventsJob?.cancel()
    }

    private fun toUserMessage(error: Throwable): String = when (error) {
        is IOException -> "No se alcanza el microservicio."
        is HttpException -> when (error.code()) {
            401, 403 -> "No tienes permiso para ver esta comunidad."
            else -> "Error del servidor: HTTP ${error.code()}"
        }
        else -> error.message ?: "Error desconocido"
    }
}
