package com.sunsettv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class Tab(val label: String) { Home("Início"), Movies("Filmes"), Live("TV ao vivo"), Series("Séries"), Settings("Ajustes") }

@Composable
fun Shell(tab: Tab, onTab: (Tab) -> Unit, content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().background(Bg).safeDrawingPadding()) {
        if (maxWidth < 600.dp) {
            Column {
                Box(Modifier.weight(1f).fillMaxWidth()) { content() }
                NavigationBar {
                    Tab.entries.forEach { t -> NavigationBarItem(selected = t == tab, onClick = { onTab(t) }, icon = { Text(when(t) { Tab.Home -> "⌂"; Tab.Movies -> "▶"; Tab.Live -> "TV"; Tab.Series -> "▤"; Tab.Settings -> "⚙" }) }, label = { Text(t.label, maxLines = 1) }) }
                }
            }
        } else {
            Row {
                Column(Modifier.width(112.dp).fillMaxHeight().verticalScroll(rememberScrollState()).padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("SUNSET TV", color = MaterialTheme.colorScheme.primary)
                    Tab.entries.forEach { t -> Pill(t.label, t == tab, Modifier.fillMaxWidth()) { onTab(t) } }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) { content() }
            }
        }
    }
}
