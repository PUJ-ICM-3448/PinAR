package com.example.pinar.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pinar.data.AuthState
import com.example.pinar.data.UserData
import com.example.pinar.ui.MainViewModel
import com.example.pinar.ui.screens.register.RegisterScreen
import com.example.pinar.ui.screens.ar.ARScreen
import com.example.pinar.ui.screens.sign.SignScreen
import com.example.pinar.ui.screens.home.HomeScreen
import com.example.pinar.ui.screens.login.LoginScreen
import com.example.pinar.ui.screens.map.MapScreen
import com.example.pinar.ui.screens.newpindetails.NewPinDetailsScreen
import com.example.pinar.ui.screens.newpinlocation.NewPinLocationScreen
import com.example.pinar.ui.screens.newpin.NewPinScreen
import com.example.pinar.ui.screens.profile.ProfileScreen
import com.example.pinar.ui.screens.notifications.NotificationsScreen

@Composable
fun NavigationStack() {

    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()
    val userData by mainViewModel.userData
    var ruta by remember{mutableStateOf(Screen.Sign.route)}

    LaunchedEffect(mainViewModel.authState.value) {
        val auth = mainViewModel.authState.value
        if (auth is AuthState.autenticado) {
            ruta = Screen.Home.route
            navController.navigate(Screen.Home.route) {
                popUpTo(0)
            }
        } else if (auth is AuthState.noAutenticado) {
            ruta = Screen.Sign.route
            if (navController.currentDestination?.route != Screen.Sign.route && 
                navController.currentDestination?.route != Screen.Login.route &&
                navController.currentDestination?.route != Screen.Register.route) {
                navController.navigate(Screen.Sign.route) {
                    popUpTo(0)
                }
            }
        }
    }


    // Estructura de navegación (NavGraph, conjunto de destinos navegables dentro de la app)
    NavHost(
        navController = navController,
        startDestination = ruta
    ) {
 

        composable(route = Screen.Sign.route) {
            SignScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = {navController.navigate(Screen.Register.route)},
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen (
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondary),
                onNavigateBack = { navController.navigate(Screen.Sign.route)},
                onNavigateToRegister = { navController.navigate(Screen.Register.route)},
                onClickLogin = { mail, contra -> mainViewModel.login(mail, contra) },
                authState = mainViewModel.authState.value
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen (
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondary),
                onNavigateBack = { navController.navigate(Screen.Sign.route)},
                onNavigateToLogin = { navController.navigate(Screen.Login.route)},
                onClickRegister = { mail, contra -> mainViewModel.registrar(mail, contra) },
                authState = mainViewModel.authState.value
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToMap = { navController.navigate(Screen.Map.route)},
                onNavigateToAR = { navController.navigate(Screen.AR.route)},
                onNavigateToProfile = { navController.navigate(Screen.Profile.route)},
                onNavigateToNewPin = { navController.navigate(Screen.NewPin.route)},
                currentScreen = Screen.Home,
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                userData = userData
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
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onClickLogout = { mainViewModel.cerrar() },
                userData = userData
            )
        }
        composable(route = Screen.NewPin.route) {
            NewPinScreen(
                onBackClick = {navController.popBackStack()},
                onCloseClick = {
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.Home.route)
                    }
                },
                onTakePhoto = {
                    navController.navigate(Screen.NewPinDetails.route)
                }
            )
        }

        composable(route = Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateToHome = { navController.navigate(Screen.Home.route) },
                onNavigateToMap = { navController.navigate(Screen.Map.route) },
                onNavigateToAR = { navController.navigate(Screen.AR.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )

        }

        composable(route = Screen.NewPinDetails.route){
            NewPinDetailsScreen(
                onBackClick = {navController.popBackStack()},
                onCloseClick = {
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.Home.route)
                    }
                },
                onContinueClick = {
                    navController.navigate(Screen.NewPinLocation.route)
                }
            )
        }
        composable(route = Screen.NewPinLocation.route){
            NewPinLocationScreen(
                onBackClick = {navController.popBackStack()},
                onCloseClick = {
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.Home.route)
                    }
                },
                onContinue = {
                    navController.navigate(Screen.Home.route){
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }
    }
}
