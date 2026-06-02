package com.example.pinar.ui.screens.home

import com.example.pinar.data.Community
import com.example.pinar.data.CommunityEvent
import com.example.pinar.data.FeedItem

data class HomeEventItem(
    val communityId: String,
    val communityName: String,
    val event: CommunityEvent
)

data class HomeState(
    val feedItems: List<FeedItem> = emptyList(),
    val recommendedCommunities: List<Community> = emptyList(),
    val activeEvents: List<HomeEventItem> = emptyList(),
    val isLoadingFeed: Boolean = false,
    val isLoadingRecommended: Boolean = false,
    val isLoadingEvents: Boolean = false,
    val feedError: String? = null,
    val recommendedError: String? = null,
)
