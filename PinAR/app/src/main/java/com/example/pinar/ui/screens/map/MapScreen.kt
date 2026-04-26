package com.example.pinar.ui.screens.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pinar.R
import com.example.pinar.navigation.Screen
import com.example.pinar.ui.utils.Footer

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    currentScreen: Screen = Screen.Map,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToAR: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit = {}
) {
    Column (modifier = modifier) {
        Spacer(modifier = Modifier.height(8.dp))
        Search(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textFieldState = rememberTextFieldState(),
            onSearch = {/*despues*/}
        )
        Map(modifier = Modifier.weight(1f).padding(horizontal = 16.dp))
        Footer(
            currentScreen = currentScreen,
            unreadCount = 3,
            onHomeClick = onNavigateToHome,
            onMapClick = onNavigateToMap,
            onARClick = onNavigateToAR,
            onNotificationsClick = onNavigateToNotifications,
            onProfileClick = onNavigateToProfile
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    onSearch: (String) -> Unit
) {
    SearchBar(
        modifier = modifier,
        query = textFieldState.text.toString(),
        onQueryChange = { query -> textFieldState.edit { replace(0, length, query) } },
        onSearch = { query -> onSearch(query) },
        active = false,
        onActiveChange = { },
        placeholder = { Text("Buscar ubicación") }
    ) {

    }
}

@Composable
fun Map(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.mapabogota),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.Red)
                .align(Alignment.TopCenter)
                .padding(0.dp, 40.dp)
        )
        
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.Blue)
                .align(Alignment.Center)
        )
    }
}
