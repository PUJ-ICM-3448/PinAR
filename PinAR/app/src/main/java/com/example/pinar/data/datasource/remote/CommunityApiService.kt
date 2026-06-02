package com.example.pinar.data.datasource.remote

import com.example.pinar.data.Community
import com.example.pinar.data.CreateCommunityRequest
import com.example.pinar.data.FeedItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CommunityApiService {

    @GET("api/v1/comunidades/feed")
    suspend fun getFeed(
        @Header("Authorization") authorization: String
    ): List<FeedItem>

    @GET("api/v1/comunidades/recomendadas")
    suspend fun getRecommendedCommunities(
        @Header("Authorization") authorization: String
    ): List<Community>

    @GET("api/v1/comunidades/{id}")
    suspend fun getCommunity(
        @Path("id") communityId: String,
        @Header("Authorization") authorization: String
    ): Community

    @POST("api/v1/comunidades/{id}/miembros")
    suspend fun joinCommunity(
        @Path("id") communityId: String,
        @Header("Authorization") authorization: String
    )

    @DELETE("api/v1/comunidades/{id}/miembros")
    suspend fun leaveCommunity(
        @Path("id") communityId: String,
        @Header("Authorization") authorization: String
    )

    @POST("api/v1/comunidades")
    suspend fun createCommunity(
        @Body request: CreateCommunityRequest,
        @Header("Authorization") authorization: String
    ): Community

    @PUT("api/v1/comunidades/{id}")
    suspend fun updateCommunity(
        @Path("id") communityId: String,
        @Body community: Community,
        @Header("Authorization") authorization: String
    ): Community

    @POST("api/v1/comunidades/{id}/pines/{pinId}")
    suspend fun sharePinWithCommunity(
        @Path("id") communityId: String,
        @Path("pinId") pinId: String,
        @Header("Authorization") authorization: String
    )

    @DELETE("api/v1/comunidades/{id}/pines/{pinId}")
    suspend fun unsharePinFromCommunity(
        @Path("id") communityId: String,
        @Path("pinId") pinId: String,
        @Header("Authorization") authorization: String
    )
}
