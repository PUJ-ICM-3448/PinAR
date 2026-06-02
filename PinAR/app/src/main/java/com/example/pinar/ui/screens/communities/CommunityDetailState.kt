package com.example.pinar.ui.screens.communities

import com.example.pinar.data.CloudAnchorPin
import com.example.pinar.data.Community

data class CommunityDetailState(
    val community: Community? = null,
    val sharedPins: List<CloudAnchorPin> = emptyList(),
    val isLoading: Boolean = true,
    val isMember: Boolean = false,
    val isJoinLeaveInProgress: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
)
