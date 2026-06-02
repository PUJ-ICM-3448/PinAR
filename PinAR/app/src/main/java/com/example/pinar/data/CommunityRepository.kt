package com.example.pinar.data

import android.content.Context
import android.net.Uri
import com.example.pinar.data.datasource.remote.CommunityApiClient
import com.example.pinar.data.datasource.remote.CommunityApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class CommunityRepository(
    private val api: CommunityApiService = CommunityApiClient.service,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
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

    suspend fun updateCommunity(community: Community): Community {
        return api.updateCommunity(
            communityId = community.id,
            community = community,
            authorization = authorizationHeader()
        ).sanitized()
    }

    suspend fun uploadCommunityImage(communityId: String, imageUri: Uri, context: Context): String {
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: throw IllegalStateException("No se pudo leer la imagen seleccionada.")
        val ref = storage.reference.child("imagenes/comunidades/${communityId}.jpg")
        ref.putBytes(bytes).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun sharePinWithCommunity(communityId: String, pinId: String) {
        api.sharePinWithCommunity(communityId, pinId, authorizationHeader())
    }

    suspend fun unsharePinFromCommunity(communityId: String, pinId: String) {
        api.unsharePinFromCommunity(communityId, pinId, authorizationHeader())
    }
}
