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

import androidx.navigation.NavType

import androidx.navigation.compose.NavHost

import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController

import androidx.navigation.navArgument

import com.example.pinar.NotificationDeepLinkHolder
import com.example.pinar.data.AuthState
import com.example.pinar.ui.MainViewModel

import com.example.pinar.ui.screens.ar.ARScreen

import com.example.pinar.ui.screens.communities.CommunitiesScreen

import com.example.pinar.ui.screens.communities.CommunityDetailScreen

import com.example.pinar.ui.screens.communities.CommunityEventScreen

import com.example.pinar.ui.screens.communities.CreateCommunityEventScreen

import com.example.pinar.ui.screens.communities.PinShareScreen

import com.example.pinar.ui.screens.home.HomeScreen

import com.example.pinar.ui.screens.login.LoginScreen

import com.example.pinar.ui.screens.map.MapScreen

import com.example.pinar.ui.screens.notifications.NotificationsScreen

import com.example.pinar.ui.screens.pindetail.PinDetailScreen

import com.example.pinar.ui.screens.pins.PinListScreen

import com.example.pinar.ui.screens.profile.EditProfileScreen

import com.example.pinar.ui.screens.profile.EditProfileViewModel

import com.example.pinar.ui.screens.profile.ProfileScreen

import com.example.pinar.ui.screens.register.RegisterScreen

import com.example.pinar.ui.screens.sign.SignScreen



@Composable

fun NavigationStack() {



    val navController = rememberNavController()

    val mainViewModel: MainViewModel = viewModel()

    val userData by mainViewModel.userData

    var ruta by remember { mutableStateOf(Screen.Sign.route) }

    val editViewModel: EditProfileViewModel = viewModel()

    LaunchedEffect(Unit) {
        NotificationDeepLinkHolder.link?.let { mainViewModel.setPendingDeepLink(it) }
        NotificationDeepLinkHolder.link = null
    }

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

                navController.currentDestination?.route != Screen.Register.route

            ) {

                navController.navigate(Screen.Sign.route) {

                    popUpTo(0)

                }

            }

        }

    }



    val pendingDeepLink by mainViewModel.pendingDeepLink

    LaunchedEffect(pendingDeepLink, userData) {
        val link = mainViewModel.consumePendingDeepLink() ?: return@LaunchedEffect
        if (userData == null) return@LaunchedEffect
        when (link) {
            is DeepLink.Pin -> navController.navigate(Screen.PinDetail.createRoute(link.pinId))
            is DeepLink.Event -> navController.navigate(
                Screen.CommunityEvent.createRoute(link.communityId, link.eventId)
            )
        }
    }



    NavHost(

        navController = navController,

        startDestination = ruta

    ) {



        composable(route = Screen.Sign.route) {

            SignScreen(

                onNavigateToLogin = { navController.navigate(Screen.Login.route) },

                onNavigateToRegister = { navController.navigate(Screen.Register.route) },

            )

        }



        composable(route = Screen.Login.route) {

            LoginScreen(

                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondary),

                onNavigateBack = { navController.navigate(Screen.Sign.route) },

                onNavigateToRegister = { navController.navigate(Screen.Register.route) },

                onClickLogin = { mail, contra -> mainViewModel.login(mail, contra) },

                authState = mainViewModel.authState.value

            )

        }



        composable(route = Screen.Register.route) {

            RegisterScreen(

                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondary),

                onNavigateBack = { navController.navigate(Screen.Sign.route) },

                onNavigateToLogin = { navController.navigate(Screen.Login.route) },

                onClickRegister = { nombre, mail, contra, biografia, fotoUri, context ->

                    mainViewModel.registrar(nombre, mail, contra, biografia, fotoUri, context)

                },

                authState = mainViewModel.authState.value

            )

        }



        composable(route = Screen.Home.route) {

            HomeScreen(

                onNavigateToMap = { navController.navigate(Screen.Map.route) },

                onNavigateToAR = { navController.navigate(Screen.AR.route) },

                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },

                currentScreen = Screen.Home,

                onNavigateToHome = { navController.navigate(Screen.Home.route) },

                onNavigateToCommunities = { navController.navigate(Screen.Communities.route) },

                onNavigateToPinDetail = { pinId ->

                    navController.navigate(Screen.PinDetail.createRoute(pinId))

                },

                onNavigateToCommunityEvent = { communityId, eventId ->

                    navController.navigate(Screen.CommunityEvent.createRoute(communityId, eventId))

                },

                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },

                userData = userData

            )

        }



        composable(route = Screen.Map.route) {

            MapScreen(

                currentScreen = Screen.Map,

                userData = userData,

                onNavigateToHome = { navController.navigate(Screen.Home.route) },

                onNavigateToMap = { navController.navigate(Screen.Map.route) },

                onNavigateToAR = { navController.navigate(Screen.AR.route) },

                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },

                onNavigateToCommunities = { navController.navigate(Screen.Communities.route) },

                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },

                onNavigateToPinDetail = { pinId ->

                    navController.navigate(Screen.PinDetail.createRoute(pinId))

                }

            )

        }



        composable(route = Screen.AR.route) {

            ARScreen(

                currentScreen = Screen.AR,

                onNavigateToHome = { navController.navigate(Screen.Home.route) },

                onNavigateToMap = { navController.navigate(Screen.Map.route) },

                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },

                onNavigateToCommunities = { navController.navigate(Screen.Communities.route) },

                onNavigateToPins = { navController.navigate(Screen.Pins.route) }

            )

        }



        composable(route = Screen.Profile.route) {

            ProfileScreen(

                currentScreen = Screen.Profile,

                onNavigateToHome = { navController.navigate(Screen.Home.route) },

                onNavigateToMap = { navController.navigate(Screen.Map.route) },

                onNavigateToAR = { navController.navigate(Screen.AR.route) },

                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },

                onNavigateToCommunities = { navController.navigate(Screen.Communities.route) },

                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },

                onClickLogout = { mainViewModel.cerrar() },

                userData = userData

            )

        }



        composable(route = Screen.Communities.route) {

            CommunitiesScreen(

                userData = userData,

                mainViewModel = mainViewModel,

                currentScreen = Screen.Communities,

                onNavigateToHome = { navController.navigate(Screen.Home.route) },

                onNavigateToMap = { navController.navigate(Screen.Map.route) },

                onNavigateToAR = { navController.navigate(Screen.AR.route) },

                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },

                onNavigateToCommunityDetail = { communityId ->

                    navController.navigate(Screen.CommunityDetail.createRoute(communityId))

                }

            )

        }



        composable(

            route = Screen.CommunityDetail.route,

            arguments = listOf(navArgument("communityId") { type = NavType.StringType })

        ) { backStackEntry ->

            val communityId = backStackEntry.arguments?.getString("communityId") ?: ""

            CommunityDetailScreen(

                communityId = communityId,

                userData = userData,

                mainViewModel = mainViewModel,

                onBackClick = { navController.popBackStack() },

                onNavigateToPinDetail = { pinId ->

                    navController.navigate(Screen.PinDetail.createRoute(pinId))

                },

                onNavigateToCreateEvent = { id ->

                    navController.navigate(Screen.CreateCommunityEvent.createRoute(id))

                },

                onNavigateToEvent = { cId, eId ->

                    navController.navigate(Screen.CommunityEvent.createRoute(cId, eId))

                },

                onNavigateToPinShare = { pinId ->

                    navController.navigate(Screen.PinShare.createRoute(pinId))

                },

                onNavigateToAR = { navController.navigate(Screen.AR.route) }

            )

        }



        composable(

            route = Screen.CreateCommunityEvent.route,

            arguments = listOf(navArgument("communityId") { type = NavType.StringType })

        ) { backStackEntry ->

            val communityId = backStackEntry.arguments?.getString("communityId") ?: ""

            CreateCommunityEventScreen(

                communityId = communityId,

                onBackClick = { navController.popBackStack() },

                onEventCreated = { eventId ->

                    navController.popBackStack()

                    navController.navigate(Screen.CommunityEvent.createRoute(communityId, eventId))

                }

            )

        }



        composable(

            route = Screen.CommunityEvent.route,

            arguments = listOf(

                navArgument("communityId") { type = NavType.StringType },

                navArgument("eventId") { type = NavType.StringType }

            )

        ) { backStackEntry ->

            val communityId = backStackEntry.arguments?.getString("communityId") ?: ""

            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""

            CommunityEventScreen(

                communityId = communityId,

                eventId = eventId,

                userData = userData,

                onBackClick = { navController.popBackStack() }

            )

        }



        composable(

            route = Screen.PinShare.route,

            arguments = listOf(navArgument("pinId") { type = NavType.StringType })

        ) { backStackEntry ->

            val pinId = backStackEntry.arguments?.getString("pinId") ?: ""

            PinShareScreen(

                pinId = pinId,

                userData = userData,

                onBackClick = { navController.popBackStack() }

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



        composable(route = Screen.Pins.route) {

            PinListScreen(

                onBackClick = { navController.popBackStack() },

                onNavigateToHome = { navController.navigate(Screen.Home.route) },

                onNavigateToMap = { navController.navigate(Screen.Map.route) },

                onNavigateToAR = { navController.navigate(Screen.AR.route) },

                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },

                onNavigateToCommunities = { navController.navigate(Screen.Communities.route) },

                onNavigateToPinDetail = { pinId ->

                    navController.navigate(Screen.PinDetail.createRoute(pinId))

                }

            )

        }



        composable(route = Screen.EditProfile.route) {

            EditProfileScreen(

                userData = userData,

                onBackClick = { navController.popBackStack() },

                viewModel = editViewModel,

                mainViewModel = mainViewModel

            )

        }



        composable(

            route = Screen.PinDetail.route,

            arguments = listOf(navArgument("pinId") { type = NavType.StringType })

        ) { backStackEntry ->

            val pinId = backStackEntry.arguments?.getString("pinId") ?: ""

            PinDetailScreen(

                pinId = pinId,

                onBackClick = { navController.popBackStack() }

            )

        }

    }

}



sealed class DeepLink {

    data class Pin(val pinId: String) : DeepLink()

    data class Event(val communityId: String, val eventId: String) : DeepLink()

}



fun handleNotificationDeepLink(type: String?, data: Map<String, String>): DeepLink? {

    return when (type) {

        "community_pin_shared" -> data["pinId"]?.let { DeepLink.Pin(it) }

        "community_event_created" -> {

            val communityId = data["communityId"] ?: return null

            val eventId = data["eventId"] ?: return null

            DeepLink.Event(communityId, eventId)

        }

        else -> null

    }

}


