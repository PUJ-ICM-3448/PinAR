package com.example.pinar.ui.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.pinar.data.CloudAnchorRepository
import com.example.pinar.data.CommunityBasicInfo
import com.example.pinar.data.CommunityEventRepository
import com.example.pinar.data.CommunityRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException
import java.io.IOException

class HomeViewModel(
    private val communityRepository: CommunityRepository = CommunityRepository(),
    private val eventRepository: CommunityEventRepository = CommunityEventRepository(),
    private val pinRepository: CloudAnchorRepository = CloudAnchorRepository()
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    fun loadHomeContent(
        memberOf: List<CommunityBasicInfo> = emptyList(),
        uid: String? = null
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoadingOwnPins = true,
                isLoadingFeed = true,
                isLoadingEvents = true,
                ownPinsError = null,
                feedError = null,
            )

            coroutineScope {
                val ownPinsDeferred = async {
                    runCatching {
                        if (uid.isNullOrBlank()) emptyList()
                        else pinRepository.getOwnPins(uid)
                    }
                }
                val feedDeferred = async {
                    runCatching { communityRepository.getFeed() }
                }
                val eventsDeferred = async {
                    runCatching { loadActiveEvents(memberOf) }
                }

                val ownPinsResult = ownPinsDeferred.await()
                val feedResult = feedDeferred.await()
                val eventsResult = eventsDeferred.await()

                val ownPinsError = ownPinsResult.exceptionOrNull()?.let { toUserMessage("tus pines", it) }
                val feedError = feedResult.exceptionOrNull()?.let { toUserMessage("feed", it) }

                _state.value = _state.value.copy(
                    ownPins = ownPinsResult.getOrElse { emptyList() },
                    ownPinsError = ownPinsError,
                    isLoadingOwnPins = false,
                    feedItems = feedResult.getOrElse { emptyList() },
                    feedError = feedError,
                    isLoadingFeed = false,
                    activeEvents = eventsResult.getOrElse { emptyList() },
                    isLoadingEvents = false,
                )
            }
        }
    }

    private suspend fun loadActiveEvents(memberOf: List<CommunityBasicInfo>): List<HomeEventItem> {
        if (memberOf.isEmpty()) return emptyList()
        return coroutineScope {
            memberOf.take(5).map { community ->
                async {
                    val events = withTimeoutOrNull(3000L) {
                        eventRepository.observeActiveEvents(community.id).first()
                    }.orEmpty()
                    events.map { event ->
                        HomeEventItem(
                            communityId = community.id,
                            communityName = community.name,
                            event = event
                        )
                    }
                }
            }.awaitAll().flatten().take(10)
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
