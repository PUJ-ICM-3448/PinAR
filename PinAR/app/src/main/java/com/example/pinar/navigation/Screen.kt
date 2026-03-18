package com.example.pinar.navigation

sealed class Screen(val route: String) {
    object Sign : Screen("sign")

    object Login : Screen("login")
    object Home : Screen("home")

    object Map : Screen("map")

    object AR : Screen("ar")

    object Profile : Screen("profile")

    object Notifications : Screen("notifications")
}
