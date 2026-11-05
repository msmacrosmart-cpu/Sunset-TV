package com.sunsettv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.sunsettv.data.Channel
import com.sunsettv.data.Library
import com.sunsettv.data.Media
import com.sunsettv.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("sunsettv", 0) }
    var lib by remember { mutableStateOf<Library?>(null) }
    var tab by remember { mutableStateOf(Tab.Home) }
    var detail by remember { mutableStateOf<Media?>(null) }
    var playing by remember { mutableStateOf<Channel?>(null) }
    var recent by remember { mutableStateOf<List<Media>>(emptyList()) }
    var favs by remember { mutableStateOf(prefs.getStringSet("favs", emptySet()).orEmpty().toSet()) }

    fun key(m: Media) = (if (m.series) "s:" else "m:") + m.id
    fun toggleFav(m: Media) {
        favs = if (key(m) in favs) favs - key(m) else favs + key(m)
        prefs.edit().putStringSet("favs", favs).apply()
    }
    fun play(m: Media, url: String, title: String) {
        recent = (listOf(m) + recent.filter { it.id != m.id }).take(15)
        playing = Channel(title, url, m.poster, m.category)
    }

    MaterialTheme(colorScheme = darkColorScheme(background = Bg, surface = CardBg)) {
        BackHandler(playing != null) { playing = null }
        BackHandler(playing == null && detail != null) { detail = null }
        val p = playing; val l = lib
        when {
            p != null -> PlayerScreen(p)
            l == null -> LoginScreen { lib = it }
            else -> Shell(tab, { tab = it; detail = null }) {
                val d = detail
                if (d != null) {
                    DetailScreen(d, l.api, key(d) in favs, { toggleFav(d) }) { url, title -> play(d, url, title) }
                } else when (tab) {
                    Tab.Home -> HomeScreen(l.live) { playing = it }
                    Tab.Movies -> BrowseScreen("Movies", l.movies, recent.filter { !it.series }) { detail = it }
                    Tab.Series -> BrowseScreen("Series", l.series, recent.filter { it.series }) { detail = it }
                    Tab.Live -> LiveScreen(l) { playing = it }
                }
            }
        }
    }
}
