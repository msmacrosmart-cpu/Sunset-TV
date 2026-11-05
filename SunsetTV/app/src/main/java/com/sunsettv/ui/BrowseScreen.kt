package com.sunsettv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sunsettv.data.Media

@Composable
fun BrowseScreen(title: String, items: List<Media>, recent: List<Media>, onPlay: (Media) -> Unit) {
    var category by remember(items) { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    val categories = remember(items) { items.map { it.category }.distinct() }
    val shown = remember(items, category, query) {
        items.filter { (category == null || it.category == category) && it.name.contains(query, true) }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
        OutlinedTextField(query, { query = it }, label = { Text("Buscar por título") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(4.dp)) {
            item { Pill("Todas (${items.size})", category == null) { category = null } }
            items(categories) { cat -> Pill(cat, cat == category) { category = cat } }
        }
        Text("${shown.size} títulos", color = Muted)
        if (shown.isEmpty()) Text("Nenhum conteúdo encontrado.", color = Muted)
        LazyVerticalGrid(GridCells.Adaptive(130.dp), Modifier.weight(1f), contentPadding = PaddingValues(6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(shown) { media ->
                Column {
                    Box(Modifier.fillMaxWidth().aspectRatio(2f / 3f).tvClickable { onPlay(media) }.background(CardBg), Alignment.Center) {
                        AsyncImage(media.poster, media.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        if (media.poster.isNullOrBlank()) Text(media.name.take(2), color = Muted)
                    }
                    Text(media.name, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
