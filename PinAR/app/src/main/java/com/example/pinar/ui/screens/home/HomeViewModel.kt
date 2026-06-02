package com.example.pinar.ui.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.pinar.data.CommunityRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class HomeViewModel(
    private val communityRepository: CommunityRepository = CommunityRepository()
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        loadHomeContent()
    }

    fun loadHomeContent() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoadingFeed = true,
                isLoadingRecommended = true,
                feedError = null,
                recommendedError = null,
            )

            coroutineScope {
                val feedDeferred = async {
                    runCatching { communityRepository.getFeed() }
                }
                val recommendedDeferred = async {
                    runCatching { communityRepository.getRecommendedCommunities() }
                }

                val feedResult = feedDeferred.await()
                val recommendedResult = recommendedDeferred.await()

                val feedError = feedResult.exceptionOrNull()?.let { toUserMessage("feed", it) }
                val recommendedError = recommendedResult.exceptionOrNull()?.let { toUserMessage("recomendadas", it) }

                _state.value = _state.value.copy(
                    feedItems = feedResult.getOrElse { emptyList() },
                    feedError = feedError,
                    isLoadingFeed = false,
                    recommendedCommunities = recommendedResult.getOrElse { emptyList() },
                    recommendedError = recommendedError,
                    isLoadingRecommended = false,
                )
            }
        }
    }

    private fun toUserMessage(endpoint: String, error: Throwable): String {
        Log.e("HomeViewModel", "Error cargando $endpoint", error)
        return when (error) {
            is IOException -> "No se alcanza el microservicio. Revisa COMMUNITY_API_BASE_URL en local.properties " +
                    "(emulador: http://10.0.2.2:8080/, teléfono: IP de tu PC en la misma WiFi)."
            is HttpException -> when (error.code()) {
                401, 403 -> "Sesión no válida para el API ($endpoint). Vuelve a iniciar sesión."
                else -> "Error del servidor ($endpoint): HTTP ${error.code()}"
            }
            else -> error.message ?: "Error desconocido"
        }
    }
}
