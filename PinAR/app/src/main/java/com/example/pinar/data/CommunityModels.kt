package com.example.pinar.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

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

data class CommunityEvent(
    val id: String = "",
    val communityId: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp? = null,
    val startsAt: Timestamp? = null,
    val endsAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    @field:PropertyName("isActive")
    val isActive: Boolean = true,
    val participants: List<String> = emptyList()
)

data class LiveLocation(
    val uid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val updatedAt: Timestamp? = null,
    val expiresAt: Timestamp? = null
)

private fun String?.orEmptyIfNull(): String = this ?: ""

/**
 * Gson/Firestore may deserialize JSON nulls into null despite non-null [String] types.
 */
fun Community.sanitized(): Community = copy(
    id = (id as String?).orEmptyIfNull(),
    name = (name as String?).orEmptyIfNull(),
    description = (description as String?).orEmptyIfNull(),
    imageUrl = (imageUrl as String?).orEmptyIfNull(),
    createdBy = (createdBy as String?).orEmptyIfNull(),
    members = members ?: emptyList(),
)

fun CommunityBasicInfo.sanitized(): CommunityBasicInfo = copy(
    id = (id as String?).orEmptyIfNull(),
    name = (name as String?).orEmptyIfNull(),
    imgUrl = (imgUrl as String?).orEmptyIfNull(),
    description = (description as String?).orEmptyIfNull(),
)

fun FeedItem.sanitized(): FeedItem = copy(
    pinId = (pinId as String?).orEmptyIfNull(),
    pinTitle = (pinTitle as String?).orEmptyIfNull(),
    pinDescription = (pinDescription as String?).orEmptyIfNull(),
    communityId = (communityId as String?).orEmptyIfNull(),
    communityName = (communityName as String?).orEmptyIfNull(),
    createdByUid = (createdByUid as String?).orEmptyIfNull(),
    imageUrl = (imageUrl as String?).orEmptyIfNull(),
)
