package com.example.pinar.ui.screens.home

import com.example.pinar.data.Community
import com.example.pinar.data.FeedItem

data class HomeState(
    val feedItems: List<FeedItem> = emptyList(),
    val recommendedCommunities: List<Community> = emptyList(),
    val isLoadingFeed: Boolean = false,
    val isLoadingRecommended: Boolean = false,
    val feedError: String? = null,
    val recommendedError: String? = null,
)
