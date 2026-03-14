package com.example.pinar.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pinar.ui.SignScreen
import com.example.pinar.ui.HomeScreen
import com.example.pinar.ui.MapScreen

@Composable
fun NavigationStack() {

    val navController = rememberNavController()


    // Estructura de navegación (NavGraph, conjunto de destinos navegables dentro de la app)
    NavHost(
        navController = navController,
        startDestination = Screen.Sign.route
    ) {

        composable(route = Screen.Sign.route) {
            SignScreen(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondary),
                onUserClick = { navController.navigate(Screen.Home.route) }
                //onUserClick = { user ->
                    //navController.navigate(Screen.Detail.route + "?userId=${user.id}")},
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToMap = { navController.navigate(Screen.Map.route) }
            )
        }
        composable(route = Screen.Map.route) {
            MapScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) }
            )
        }
    }
}