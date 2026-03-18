package com.example.pinar.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pinar.ui.screens.ARScreen
import com.example.pinar.ui.screens.SignScreen
import com.example.pinar.ui.screens.HomeScreen
import com.example.pinar.ui.screens.LogInScreen
import com.example.pinar.ui.screens.MapScreen
import com.example.pinar.ui.screens.NewPinScreen
import com.example.pinar.ui.screens.ProfileScreen
import com.example.pinar.ui.screens.NotificationsScreen

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
                onUserClick = { navController.navigate(Screen.Login.route) }
                //onUserClick = { user ->
                    //navController.navigate(Screen.Detail.route + "?userId=${user.id}")},
            )
        }

        composable(route = Screen.Login.route) {
            LogInScreen (
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondary),
                onNavigateBack = { navController.navigate(Screen.Sign.route)},
                onNavigateToHome = { navController.navigate(Screen.Home.route)},
                onNavigateToRegister = { navController.navigate(Screen.Sign.route)},

            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToMap = { navController.navigate(Screen.Map.route)},
                onNavigateToAR = { navController.navigate(Screen.AR.route)},
                onNavigateToProfile = { navController.navigate(Screen.Profile.route)},
                onNavigateToNewPin = { navController.navigate(Screen.NewPin.route)}
                currentScreen = Screen.Home,
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToAR = { navController.navigate(Screen.AR.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
            )
        }
        composable(route = Screen.Map.route) {
            MapScreen(
                currentScreen = Screen.Map,
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToAR = { navController.navigate(Screen.AR.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
            )
        }
        composable(route = Screen.AR.route) {
            ARScreen(
                currentScreen = Screen.AR,
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
            )
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen(
                currentScreen = Screen.Profile,
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToAR = { navController.navigate(Screen.AR.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
            )
        }
        composable(route = Screen.NewPin.route) {
            NewPinScreen()
        }

        composable(route = Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToAR = { navController.navigate(Screen.AR.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )

        }
    }
}