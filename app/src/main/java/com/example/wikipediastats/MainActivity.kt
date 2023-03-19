package com.example.wikipediastats

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.example.wikipediastats.ui.theme.WikipediaStatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WikipediaStatsTheme {
                val window: Window = this.window
                WindowCompat.setDecorFitsSystemWindows(window, false)

                val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)

                windowInsetsController?.isAppearanceLightNavigationBars = true

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SearchStats()
                }
            }
        }
    }
}

@Composable
fun SearchStats() {
    Box(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
        Row(
            Modifier
                .align(Alignment.TopCenter)
                .padding(all = 8.dp)) {
            var text by remember { mutableStateOf(TextFieldValue("")) }
            TextField(
                value = text,
                onValueChange = { newText ->
                    text = newText
                },
                label = { Text("Article") },
            )
            
            Button(onClick = {  }) {
                
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WikipediaStatsTheme {
        //Greeting("Android")
    }
}
