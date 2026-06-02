package com.example.pinar.navigation

sealed class Screen(val route: String) {
    object Sign : Screen("sign")

    object Login : Screen("login")

    object Register : Screen("register")
    object Home : Screen("home")

    object Map : Screen("map")

    object AR : Screen("ar")

    object Profile : Screen("profile")

    object Notifications : Screen("notifications")

    object Communities : Screen("communities")

    object CommunityDetail : Screen("community_detail/{communityId}") {
        fun createRoute(communityId: String) = "community_detail/$communityId"
    }

    object EditProfile : Screen("edit_profile")

    object Pins : Screen("pins")

    object PinDetail : Screen("pin_detail/{pinId}") {
        fun createRoute(pinId: String) = "pin_detail/$pinId"
    }

    object CreateCommunityEvent : Screen("create_community_event/{communityId}") {
        fun createRoute(communityId: String) = "create_community_event/$communityId"
    }

    object CommunityEvent : Screen("community_event/{communityId}/{eventId}") {
        fun createRoute(communityId: String, eventId: String) =
            "community_event/$communityId/$eventId"
    }

    object PinShare : Screen("pin_share/{pinId}") {
        fun createRoute(pinId: String) = "pin_share/$pinId"
    }
}
