package com.example.pinar.ui.screens.communities

import com.example.pinar.data.Community
import com.example.pinar.data.CommunityBasicInfo

data class CommunitiesState(
    val myCommunities: List<CommunityBasicInfo> = emptyList(),
    val recommended: List<Community> = emptyList(),
    val isLoadingRecommended: Boolean = false,
    val recommendedError: String? = null,
    val isCreating: Boolean = false,
    val actionMessage: String? = null,
)
