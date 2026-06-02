package com.example.pinar.ui.screens.home

import com.example.pinar.data.CloudAnchorPin
import com.example.pinar.data.CommunityEvent
import com.example.pinar.data.FeedItem

data class HomeEventItem(
    val communityId: String,
    val communityName: String,
    val event: CommunityEvent
)

data class HomeState(
    val ownPins: List<CloudAnchorPin> = emptyList(),
    val feedItems: List<FeedItem> = emptyList(),
    val activeEvents: List<HomeEventItem> = emptyList(),
    val isLoadingOwnPins: Boolean = false,
    val isLoadingFeed: Boolean = false,
    val isLoadingEvents: Boolean = false,
    val ownPinsError: String? = null,
    val feedError: String? = null,
)
