package com.sunsettv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sunsettv.data.Media

/** Usada por MOVIES e SERIES (mesmo layout, muda título e dados). */
@Composable
fun BrowseScreen(title: String, items: List<Media>, recent: List<Media>, onPlay: (Media) -> Unit) {
    val cats = remember(items) { listOf("All Categories") + items.map { it.category }.distinct().take(30) }
    var sel by remember { mutableStateOf(0) }
    val shown = remember(items, sel) { if (sel == 0) items else items.filter { it.category == cats[sel] } }
    val padEnd = PaddingValues(end = 32.dp, top = 6.dp, bottom = 6.dp)

    LazyColumn(contentPadding = PaddingValues(start = 32.dp, top = 28.dp, bottom = 32.dp)) {
        item {
            Column {
                Text(title.uppercase(), color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                Text("Browse and search within your collection", color = Muted, fontSize = 15.sp)
                Spacer(Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(end = 32.dp, top = 4.dp, bottom = 4.dp)) {
                    itemsIndexed(cats) { i, c -> Pill(c, i == sel) { sel = i } }
                }
            }
        }
        if (items.isEmpty()) item { Text("Nenhum conteúdo disponível nesta lista.", color = Muted, modifier = Modifier.padding(top = 32.dp)) }
        else {
            item { Title("All $title") }
            item { LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = padEnd) { items(shown.take(80)) { Poster(it, onPlay) } } }
            if (recent.isNotEmpty()) {
                item { Title("Continue Watching") }
                item { LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = padEnd) { items(recent) { Wide(it, onPlay) } } }
            }
            item { Title("Recently Added") }
            item { LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = padEnd) { items(items.takeLast(30).reversed()) { Wide(it, onPlay) } } }
        }
    }
}

@Composable
private fun Poster(m: Media, onPlay: (Media) -> Unit) {
    Column(Modifier.width(150.dp)) {
        Box(Modifier.size(150.dp, 215.dp).tvClickable { onPlay(m) }.background(CardBg), Alignment.Center) {
            AsyncImage(m.poster, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (m.poster == null) Text(m.name.take(2).uppercase(), color = Muted, fontSize = 24.sp)
        }
        Text(m.name, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun Wide(m: Media, onPlay: (Media) -> Unit) {
    Column(Modifier.width(230.dp)) {
        Box(Modifier.size(230.dp, 96.dp).tvClickable { onPlay(m) }.background(CardBg)) {
            AsyncImage(m.poster, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter)
        }
        Text(m.name, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
    }
}
