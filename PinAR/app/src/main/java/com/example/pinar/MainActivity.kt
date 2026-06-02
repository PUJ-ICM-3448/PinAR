package com.example.pinar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pinar.navigation.NavigationStack
import com.example.pinar.navigation.handleNotificationDeepLink
import com.example.pinar.ui.MainViewModel
import com.example.pinar.ui.theme.PinARTheme
import com.google.android.gms.maps.MapsInitializer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) { }

        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            PinARTheme(darkTheme = isSystemInDarkTheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationStack()
                }
            }
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val extras = intent?.extras ?: return
        val type = extras.getString("type")
        val data = buildMap {
            extras.keySet().forEach { key ->
                extras.getString(key)?.let { put(key, it) }
            }
        }
        val link = handleNotificationDeepLink(type, data) ?: return
        // ViewModel is created in setContent; store link for next composition via static holder
        NotificationDeepLinkHolder.link = link
    }
}

object NotificationDeepLinkHolder {
    var link: com.example.pinar.navigation.DeepLink? = null
}
