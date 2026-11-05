package com.sunsettv.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sunsettv.data.Channel

@Composable
fun HomeScreen(channels: List<Channel>, onPlay: (Channel) -> Unit) {
    val groups = remember(channels) { channels.groupBy { it.group }.entries.take(10) }
    val hero = channels.firstOrNull()
    Row(Modifier.fillMaxSize().background(Bg)) {
        Box(Modifier.weight(1f).fillMaxHeight()) {
            LazyColumn(contentPadding = PaddingValues(start = 32.dp, top = 24.dp, bottom = 32.dp)) {
                if (hero != null) item { Hero(hero, onPlay) }
                item { Title("Ao vivo agora") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(end = 32.dp, top = 6.dp, bottom = 6.dp)) {
                        items(channels.take(20)) { LiveCard(it, onPlay) }
                    }
                }
                groups.forEach { (g, list) ->
                    item { Title(g) }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(end = 32.dp, top = 6.dp, bottom = 6.dp)) {
                            items(list.take(30)) { PosterCard(it, onPlay) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Hero(ch: Channel, onPlay: (Channel) -> Unit) {
    Box(Modifier.fillMaxWidth().height(250.dp)) {
        AsyncImage(
            ch.logo, null, Modifier.align(Alignment.CenterEnd).fillMaxWidth(0.4f).fillMaxHeight().alpha(0.55f),
            contentScale = ContentScale.Fit
        )
        Column(Modifier.fillMaxWidth(0.55f).align(Alignment.CenterStart), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("CANAL • FAVORITO", color = Muted, fontSize = 13.sp)
            Text(ch.name, color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(ch.group, color = Muted, fontSize = 15.sp)
            Spacer(Modifier.height(6.dp))
            Pill("▶  Assistir", true) { onPlay(ch) }
        }
    }
}

@Composable
fun Title(t: String) =
    Text(t, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))

@Composable
private fun PosterCard(ch: Channel, onPlay: (Channel) -> Unit) {
    Column(Modifier.width(150.dp)) {
        Box(Modifier.size(150.dp, 92.dp).tvClickable { onPlay(ch) }.background(CardBg), Alignment.Center) {
            AsyncImage(ch.logo, null, Modifier.fillMaxSize().padding(10.dp), contentScale = ContentScale.Fit)
            if (ch.logo == null) Text(ch.name.take(2).uppercase(), color = Muted, fontSize = 22.sp)
        }
        Text(ch.name, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun LiveCard(ch: Channel, onPlay: (Channel) -> Unit) {
    Row(
        Modifier.width(240.dp).tvClickable { onPlay(ch) }.background(CardBg).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(ch.logo, null, Modifier.size(48.dp), contentScale = ContentScale.Fit)
        Column {
            Text(ch.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("● LIVE", color = Color(0xFFFF4D5E), fontSize = 11.sp)
        }
    }
}
