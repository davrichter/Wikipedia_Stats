package com.example.wikipediastats

import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import com.example.wikipediastats.ui.theme.WikipediaStatsTheme
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    SearchStats()
                }
            }
        }
    }
}


@OptIn(DelicateCoroutinesApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchStats() {
    Box(
        Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .fillMaxSize()
            .widthIn(min = 320.dp)
    ) {
        var text by remember { mutableStateOf("") }
        Column(
            Modifier
                .align(Alignment.TopCenter)
                .padding(start = 8.dp)
        ) {
            Row() {
                TextField(
                    modifier = Modifier.padding(all = 8.dp),
                    value = text,
                    onValueChange = { newText ->
                        text = newText
                    },
                    label = { Text("Article") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Go
                    )
                )

                Button(modifier = Modifier.align(Alignment.CenterVertically),
                    onClick = { GlobalScope.launch { getStatistics(text) } }) {
                    Text("Go")
                }
            }

            var startDate by remember {
                mutableStateOf(LocalDate.now())
            }

            val endDate by remember {
                mutableStateOf(LocalDate.now())
            }

            Row {
                Box(Modifier.padding(all = 8.dp)) {
                    TimePeriodPicker("Start", startDate, onDateUpdate = {
                        Log.i("MyApp", "Start: $it")
                        startDate = it
                    })
                }
                Box(Modifier.padding(all = 8.dp)) {
                    TimePeriodPicker("End", endDate, onDateUpdate = {
                        Log.i("MyApp", "End: $it")
                        startDate = it
                    })
                }
            }

            if (text.isNotBlank()) {
                ShowStats(text)
            }
        }
    }
}


@Composable
fun TimePeriodPicker(label: String, beginDate: LocalDate, onDateUpdate: (LocalDate) -> Unit = {}) {
    val dateDialogState = rememberMaterialDialogState()

    Button(onClick = {
        dateDialogState.show()
    }) {
        Text(text = label)
    }

    var date by remember { mutableStateOf(beginDate) }

    MaterialDialog(dialogState = dateDialogState, buttons = {
        positiveButton(text = "Ok")
        negativeButton(text = "Cancel")
    }) {
        datepicker(
            initialDate = LocalDate.now(),
            title = "Pick a ${label}date",
        ) {
            date = it
            onDateUpdate(it)
        }
    }
}

@Composable
fun ShowStats(article: String) {
    var statistics by remember { mutableStateOf<List<Item>?>(null) }

    LaunchedEffect(article) {
        statistics = withContext(Dispatchers.IO) { getStatistics(article) }
    }

    // The last two digits may be hours but are always zero so they are later just sliced away
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val printFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    statistics?.let { stats ->
        Box(Modifier.padding(start = 8.dp)) {
            LazyColumn {
                items(stats.size) { item ->
                    Text(
                        text = "$article - ${
                            LocalDate.parse(stats[item].timestamp.slice(0..7), formatter)
                                .format(printFormatter)
                        }: ${
                            stats[item].views
                        }"
                    )
                }
            }
        }
    }
}


val client = OkHttpClient()

fun getStatistics(title: String?): List<Item>? {
    val url =
        "https://wikimedia.org/api/rest_v1/metrics/pageviews/per-article/en.wikipedia/all-access/all-agents/$title/daily/2015100100/2015103100"
    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    return response.body()?.string()?.let {
        if (JSONObject(it).has("items")) {
            Json.decodeFromString<List<Item>>(JSONObject(it)["items"].toString())
        } else {
            null
        }
    }
}

@Serializable
data class Item(
    val project: String,
    val article: String,
    val granularity: String,
    val timestamp: String,
    val access: String,
    val agent: String,
    val views: Int,
)

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WikipediaStatsTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            //SearchStats()
        }
    }
}
