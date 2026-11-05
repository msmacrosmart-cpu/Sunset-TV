package com.sunsettv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sunsettv.data.Channel
import com.sunsettv.data.Epg
import com.sunsettv.data.Library
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Panel = Color(0x12FFFFFF)
private val hm = SimpleDateFormat("HH:mm", Locale.getDefault())
private fun t(ms: Long) = hm.format(Date(ms))

@Composable
fun LiveScreen(lib: Library, onPlay: (Channel) -> Unit) {
    val cats = remember(lib) { lib.live.map { it.group }.distinct() }
    var cat by remember { mutableStateOf(cats.firstOrNull() ?: "") }
    val list = remember(cat, lib) { lib.live.filter { it.group == cat } }
    var ch by remember { mutableStateOf<Channel?>(null) }
    var epg by remember { mutableStateOf<List<Epg>>(emptyList()) }
    val api = lib.api

    LaunchedEffect(list) { ch = list.firstOrNull() }
    LaunchedEffect(ch) {
        epg = emptyList()
        val id = ch?.id
        if (api != null && id != null) epg = runCatching { api.epg(id) }.getOrDefault(emptyList())
    }

    Row(Modifier.fillMaxSize().padding(24.dp), Arrangement.spacedBy(16.dp)) {
        // Coluna 1: categorias
        Column(Modifier.width(210.dp).fillMaxHeight().background(Panel, RoundedCornerShape(16.dp)).padding(12.dp)) {
            Text("CATEGORIAS", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(cats) { c ->
                    Box(
                        Modifier.fillMaxWidth().onFocusChanged { if (it.hasFocus) cat = c }
                            .tvClickable(RoundedCornerShape(10.dp)) { cat = c }
                            .background(if (c == cat) Color(0x33FF4D5E) else Color.Transparent).padding(12.dp)
                    ) { Text(c, color = Color.White, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                }
            }
        }
        // Coluna 2: canais
        Column(Modifier.width(330.dp).fillMaxHeight().background(Panel, RoundedCornerShape(16.dp)).padding(12.dp)) {
            Text("CANAIS", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(list) { i, c ->
                    Row(
                        Modifier.fillMaxWidth().onFocusChanged { if (it.hasFocus) ch = c }
                            .tvClickable(RoundedCornerShape(10.dp)) { onPlay(c) }
                            .background(if (c == ch) Color(0x22FFFFFF) else Color.Transparent).padding(10.dp),
                        Arrangement.spacedBy(10.dp), Alignment.CenterVertically
                    ) {
                        Text("%03d".format(i + 1), color = Muted, fontSize = 13.sp)
                        AsyncImage(c.logo, null, Modifier.size(36.dp), contentScale = ContentScale.Fit)
                        Text(c.name, color = Color.White, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        // Coluna 3: EPG
        Column(Modifier.weight(1f).fillMaxHeight().background(Panel, RoundedCornerShape(16.dp)).padding(24.dp), Arrangement.spacedBy(10.dp)) {
            Text(ch?.name ?: "", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            val now = System.currentTimeMillis()
            val cur = epg.firstOrNull { now in it.start..it.end }
            if (epg.isEmpty()) Text("Guia de programação indisponível para este canal.", color = Muted, fontSize = 16.sp)
            cur?.let {
                Text("AGORA", color = Color(0xFFFFB347), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(it.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                Text("${t(it.start)} – ${t(it.end)}", color = Muted, fontSize = 15.sp)
                Box(Modifier.fillMaxWidth().height(4.dp).background(Color(0x22FFFFFF))) {
                    Box(Modifier.fillMaxWidth(((now - it.start).toFloat() / (it.end - it.start)).coerceIn(0f, 1f)).fillMaxHeight().background(Sunset))
                }
                if (it.desc.isNotBlank()) Text(it.desc, color = Color(0xFFD0D0D6), fontSize = 16.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            }
            val next = epg.filter { it.start > now }
            if (next.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("A SEGUIR", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                next.take(5).forEach {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(t(it.start), color = Color(0xFFFFB347), fontSize = 15.sp)
                        Text(it.title, color = Color.White, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
