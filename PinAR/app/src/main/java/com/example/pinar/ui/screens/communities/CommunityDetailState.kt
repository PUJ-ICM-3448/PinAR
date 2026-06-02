package com.example.pinar.ui.screens.communities

import com.example.pinar.data.CloudAnchorPin
import com.example.pinar.data.Community
import com.example.pinar.data.CommunityEvent

data class CommunityDetailState(
    val community: Community? = null,
    val sharedPins: List<CloudAnchorPin> = emptyList(),
    val activeEvents: List<CommunityEvent> = emptyList(),
    val isLoading: Boolean = true,
    val isMember: Boolean = false,
    val isJoinLeaveInProgress: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null,
)
