package com.example.pinar.data

data class CommunityBasicInfo(
    val id: String = "",
    val name: String = "",
    val imgUrl: String = "",
    val description: String = ""
)

data class Community(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val createdBy: String = "",
    val isPublic: Boolean = true,
    val memberCount: Int = 0,
    val members: List<String> = emptyList()
)

data class CreateCommunityRequest(
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = true,
    val imageUrl: String = ""
)

data class FeedItem(
    val pinId: String = "",
    val pinTitle: String = "",
    val pinDescription: String = "",
    val communityId: String = "",
    val communityName: String = "",
    val createdByUid: String = "",
    val createdAt: Long = 0L,
    val imageUrl: String = ""
)
