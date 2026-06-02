package com.example.pinar.data

import com.example.pinar.data.datasource.remote.CommunityApiClient
import com.example.pinar.data.datasource.remote.CommunityApiService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class CommunityRepository(
    private val api: CommunityApiService = CommunityApiClient.service,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private suspend fun authorizationHeader(): String {
        val user = auth.currentUser
            ?: throw IllegalStateException("Debes iniciar sesión para acceder a las comunidades.")
        val token = user.getIdToken(false).await().token
            ?: throw IllegalStateException("No se pudo obtener el token de autenticación.")
        return "Bearer $token"
    }

    fun currentUid(): String? = auth.currentUser?.uid

    suspend fun getFeed(): List<FeedItem> =
        api.getFeed(authorizationHeader()).map { it.sanitized() }

    suspend fun getRecommendedCommunities(): List<Community> =
        api.getRecommendedCommunities(authorizationHeader()).map { it.sanitized() }

    suspend fun getCommunity(id: String): Community =
        api.getCommunity(id, authorizationHeader()).sanitized()

    suspend fun joinCommunity(id: String) {
        api.joinCommunity(id, authorizationHeader())
    }

    suspend fun leaveCommunity(id: String) {
        api.leaveCommunity(id, authorizationHeader())
    }

    suspend fun createCommunity(
        name: String,
        description: String,
        isPublic: Boolean = true,
        imageUrl: String = ""
    ): Community {
        return api.createCommunity(
            CreateCommunityRequest(
                name = name,
                description = description,
                isPublic = isPublic,
                imageUrl = imageUrl
            ),
            authorizationHeader()
        ).sanitized()
    }

    suspend fun sharePinWithCommunity(communityId: String, pinId: String) {
        api.sharePinWithCommunity(communityId, pinId, authorizationHeader())
    }

    suspend fun unsharePinFromCommunity(communityId: String, pinId: String) {
        api.unsharePinFromCommunity(communityId, pinId, authorizationHeader())
    }
}
