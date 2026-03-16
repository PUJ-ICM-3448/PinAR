package com.example.pinar.navigation

sealed class Screen(val route: String) {
    object Sign : Screen("sign")
    object Home : Screen("home")

    object Map : Screen("map")

    object Profile : Screen("profile")
}
