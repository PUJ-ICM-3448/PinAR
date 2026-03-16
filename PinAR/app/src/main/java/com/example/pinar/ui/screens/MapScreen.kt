package com.example.pinar.ui.screens
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.material3.SearchBar
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import com.example.pinar.ui.utils.Footer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit,
    onNavigateToAR: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Column (modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))
        Search(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            textFieldState = remember { TextFieldState() },
            onSearch = {/*despues*/}
        )
        Map(modifier = Modifier.weight(1f).padding(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Footer(
            onHomeClick = onNavigateToHome,
            onARClick = onNavigateToAR,
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
        onQueryChange = { },
        onSearch = { query -> onSearch(query) },
        active = false,
        onActiveChange = { },
        placeholder = { Text("Buscar ubicación") }
    ) {

    }
}

@Composable
fun Map(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                loadUrl("https://www.openstreetmap.org/")
            }
        }
    )
}

