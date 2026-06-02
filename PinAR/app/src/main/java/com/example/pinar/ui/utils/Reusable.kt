package com.example.pinar.ui.utils

import androidx.compose.animation.core.copy
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pinar.R
import com.example.pinar.ui.theme.RedDeep
import com.example.pinar.ui.theme.RedPrimary
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Card
import com.example.pinar.navigation.Screen

@Composable
fun PinMiniLogo() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = RedPrimary,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.location),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
        )
    }
}

@Composable
fun PinArLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.pinarlogo),
        contentDescription = null,
        modifier = modifier
    )
}

@Composable
fun IconoConTexto(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = Color.Gray
        )
    }
}

@Composable
fun LogoVertical(
    modifier: Modifier = Modifier,
    icono: Int,
    text: String
) {
    Box(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(icono),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = text
            )
        }
    }
}

@Composable
fun Footer(
    modifier: Modifier = Modifier,
    currentScreen: Screen = Screen.Home,
    unreadCount: Int = 0,
    onHomeClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onARClick: () -> Unit = {},
    onCommunitiesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    val navColors = NavigationBarItemDefaults.colors(
        unselectedIconColor = Color.LightGray,
        unselectedTextColor = Color.LightGray,
        indicatorColor = Color.Transparent,
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary
    )
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.Home,
            onClick = onHomeClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.home),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Inicio", fontSize = 12.sp) },
            colors = navColors
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Map,
            onClick = onMapClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.map),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Mapa", fontSize = 12.sp) },
            colors = navColors
        )
        NavigationBarItem(
            selected = currentScreen == Screen.AR,
            onClick = onARClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.clock),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("AR", fontSize = 12.sp) },
            colors = navColors
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Communities,
            onClick = onCommunitiesClick,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Groups,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Comunidades", fontSize = 11.sp) },
            colors = navColors
        )
        NavigationBarItem(
            selected = currentScreen == Screen.Profile,
            onClick = onProfileClick,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.profile),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Perfil", fontSize = 12.sp) },
            colors = navColors
        )
    }
}

@Composable
fun TopBar(
    titulo: String,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.ArrowBack, contentDescription = "Back",
            modifier = Modifier.clickable{onBackClick()})
        Text(
            text = titulo,
            fontWeight = FontWeight.Bold
        )
        Icon(Icons.Default.Close, contentDescription = "Close",
            modifier = Modifier.clickable{onCloseClick()})
    }
}

@Composable
fun InputCard(
    titulo: String,
    contador: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
){
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier= Modifier.height(12.dp))
            content()
            if(contador != null){
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = contador,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}