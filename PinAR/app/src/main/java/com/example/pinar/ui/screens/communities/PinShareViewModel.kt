package com.example.pinar.ui.screens.communities

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pinar.data.CloudAnchorRepository
import com.example.pinar.data.CommunityBasicInfo
import com.example.pinar.data.CommunityRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class PinShareState(
    val pinTitle: String = "",
    val communities: List<CommunityBasicInfo> = emptyList(),
    val sharedCommunityIds: Set<String> = emptySet(),
    val isLoading: Boolean = true,
    val isSharing: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class PinShareViewModel(
    private val communityRepository: CommunityRepository = CommunityRepository(),
    private val pinRepository: CloudAnchorRepository = CloudAnchorRepository()
) : ViewModel() {

    private val _state = mutableStateOf(PinShareState())
    val state: State<PinShareState> = _state

    fun load(pinId: String, myCommunities: List<CommunityBasicInfo>) {
        viewModelScope.launch {
            _state.value = PinShareState(isLoading = true, communities = myCommunities)
            runCatching {
                val pin = pinRepository.getPin(pinId)
                PinShareState(
                    pinTitle = pin?.title.orEmpty().ifBlank { "Pin" },
                    communities = myCommunities,
                    sharedCommunityIds = pin?.comunidades?.toSet().orEmpty(),
                    isLoading = false
                )
            }.onSuccess { _state.value = it }
                .onFailure {
                    _state.value = PinShareState(
                        communities = myCommunities,
                        isLoading = false,
                        error = toUserMessage(it)
                    )
                }
        }
    }

    fun shareWithCommunity(communityId: String, pinId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSharing = true, message = null)
            runCatching { communityRepository.sharePinWithCommunity(communityId, pinId) }
                .onSuccess {
                    _state.value = _state.value.copy(
                        isSharing = false,
                        sharedCommunityIds = _state.value.sharedCommunityIds + communityId,
                        message = "Pin compartido"
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        isSharing = false,
                        message = toUserMessage(it)
                    )
                }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun toUserMessage(error: Throwable): String = when (error) {
        is IOException -> "No se alcanza el microservicio."
        is HttpException -> when (error.code()) {
            403 -> "No tienes permiso para compartir este pin."
            else -> "Error del servidor: HTTP ${error.code()}"
        }
        else -> error.message ?: "Error desconocido"
    }
}
