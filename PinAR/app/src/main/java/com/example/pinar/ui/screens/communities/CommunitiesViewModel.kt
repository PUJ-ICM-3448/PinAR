package com.example.pinar.ui.screens.communities

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.data.CommunityBasicInfo
import com.example.pinar.data.CommunityRepository
import com.example.pinar.ui.MainViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class CommunitiesViewModel(
    private val communityRepository: CommunityRepository = CommunityRepository()
) : ViewModel() {

    private val _state = mutableStateOf(CommunitiesState())
    val state: State<CommunitiesState> = _state

    fun syncMyCommunities(memberOf: List<CommunityBasicInfo>) {
        _state.value = _state.value.copy(myCommunities = memberOf)
    }

    fun loadRecommended(myCommunityIds: Set<String>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoadingRecommended = true,
                recommendedError = null
            )
            runCatching { communityRepository.getRecommendedCommunities() }
                .onSuccess { list ->
                    _state.value = _state.value.copy(
                        recommended = list.filter { it.id !in myCommunityIds },
                        isLoadingRecommended = false
                    )
                }
                .onFailure { error ->
                    Log.e("CommunitiesViewModel", "Error cargando recomendadas", error)
                    _state.value = _state.value.copy(
                        isLoadingRecommended = false,
                        recommendedError = toUserMessage(error)
                    )
                }
        }
    }

    fun createCommunity(
        name: String,
        description: String,
        isPublic: Boolean,
        imageUri: Uri?,
        context: Context,
        mainViewModel: MainViewModel,
        onCreated: (String) -> Unit
    ) {
        if (name.isBlank()) {
            _state.value = _state.value.copy(actionMessage = "El nombre es obligatorio")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreating = true, actionMessage = null)
            runCatching {
                var created = communityRepository.createCommunity(
                    name = name.trim(),
                    description = description.trim(),
                    isPublic = isPublic
                )
                if (imageUri != null) {
                    val imageUrl = communityRepository.uploadCommunityImage(
                        created.id,
                        imageUri,
                        context
                    )
                    created = communityRepository.updateCommunity(
                        created.copy(
                            name = name.trim(),
                            description = description.trim(),
                            isPublic = isPublic,
                            imageUrl = imageUrl
                        )
                    )
                }
                mainViewModel.updateCommunityInMemberOf(
                    communityId = created.id,
                    name = created.name,
                    description = created.description,
                    imgUrl = created.imageUrl
                )
                mainViewModel.refreshUserData(fromServer = true)
                created.id
            }
                .onSuccess { communityId ->
                    _state.value = _state.value.copy(isCreating = false)
                    onCreated(communityId)
                }
                .onFailure { error ->
                    Log.e("CommunitiesViewModel", "Error creando comunidad", error)
                    _state.value = _state.value.copy(
                        isCreating = false,
                        actionMessage = toUserMessage(error)
                    )
                }
        }
    }

    fun clearActionMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }

    private fun toUserMessage(error: Throwable): String = when (error) {
        is IOException -> "No se alcanza el microservicio. Revisa que esté en ejecución y la URL en local.properties."
        is HttpException -> when (error.code()) {
            401, 403 -> "No tienes permiso para esta acción."
            else -> "Error del servidor: HTTP ${error.code()}"
        }
        else -> error.message ?: "Error desconocido"
    }
}
