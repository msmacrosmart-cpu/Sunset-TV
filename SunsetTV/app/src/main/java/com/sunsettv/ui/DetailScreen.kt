package com.sunsettv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sunsettv.data.Detail
import com.sunsettv.data.Media
import com.sunsettv.data.Xtream
import com.sunsettv.data.detail
import kotlinx.coroutines.delay

@Composable
fun DetailScreen(m: Media, api: Xtream?, fav: Boolean, onFav: () -> Unit, onPlay: (url: String, title: String) -> Unit) {
    var d by remember(m) { mutableStateOf<Detail?>(null) }
    var sIdx by remember(m) { mutableStateOf(0) }
    val fr = remember { FocusRequester() }
    LaunchedEffect(m) { d = if (api != null) runCatching { api.detail(m) }.getOrNull() else null }
    LaunchedEffect(m) { delay(200); runCatching { fr.requestFocus() } } // foco inicial no Play

    val seasons = d?.seasons.orEmpty()
    val eps = seasons.getOrNull(sIdx)?.episodes.orEmpty()
    val playUrl = if (m.series) seasons.firstOrNull()?.episodes?.firstOrNull()?.url else m.url
    val rating = d?.rating?.takeIf { it.isNotBlank() && it != "0" }
    val info = listOfNotNull(rating?.let { "★ $it" }, (d?.genre?.ifBlank { null } ?: m.category), d?.year?.ifBlank { null })
        .joinToString("   |   ")

    Box(Modifier.fillMaxSize()) {
        // Lado direito: hero image com transição suave para o fundo
        AsyncImage(
            d?.backdrop ?: d?.poster ?: m.poster, null,
            Modifier.align(Alignment.CenterEnd).fillMaxHeight().fillMaxWidth(0.6f),
            contentScale = ContentScale.Crop, alignment = Alignment.TopCenter
        )
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0.30f to Bg, 0.55f to Bg.copy(alpha = 0.75f), 1f to Color.Transparent)))
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.75f to Color.Transparent, 1f to Bg)))

        // Lado esquerdo: informações
        LazyColumn(
            Modifier.fillMaxHeight().fillMaxWidth(0.62f),
            contentPadding = PaddingValues(start = 32.dp, top = 40.dp, bottom = 40.dp, end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text(m.name, color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis) }
            item { Text(info, color = Color(0xFFFFB347), fontSize = 16.sp) }
            item {
                Text(
                    d?.plot?.ifBlank { null } ?: if (d == null && api != null) "Carregando detalhes..." else "Sinopse não disponível.",
                    color = Color(0xFFD0D0D6), fontSize = 16.sp, maxLines = 6, overflow = TextOverflow.Ellipsis
                )
            }
            item {
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Pill("▶  Assistir", true, Modifier.focusRequester(fr)) { playUrl?.let { onPlay(it, m.name) } }
                    Pill(if (fav) "★  Nos favoritos" else "☆  Favoritos", false) { onFav() }
                }
            }
            if (m.series && seasons.isNotEmpty()) {
                item { Title("Temporadas") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                        itemsIndexed(seasons) { i, s -> Pill("Season ${s.number}", i == sIdx) { sIdx = i } }
                    }
                }
                item { Title("Episódios") }
                items(eps) { e ->
                    Row(
                        Modifier.fillMaxWidth().tvClickable(RoundedCornerShape(10.dp)) { onPlay(e.url, "${m.name} • ${e.num}. ${e.title}") }
                            .background(CardBg).padding(8.dp),
                        Arrangement.spacedBy(14.dp), Alignment.CenterVertically
                    ) {
                        AsyncImage(e.thumb ?: m.poster, null, Modifier.size(128.dp, 72.dp).clip(RoundedCornerShape(6.dp)).background(Bg), contentScale = ContentScale.Crop)
                        Text("${e.num}", color = Color(0xFFFFB347), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(e.title, color = Color.White, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
