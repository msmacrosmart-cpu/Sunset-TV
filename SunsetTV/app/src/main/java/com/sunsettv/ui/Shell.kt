package com.sunsettv.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class Tab { Home, Movies, Live, Series }

/** Menu lateral fixo (centralizado) + fundo com risca neon, compartilhados por todas as telas. */
@Composable
fun Shell(tab: Tab, onTab: (Tab) -> Unit, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxSize().background(Bg)) {
        Sidebar(tab, onTab)
        Box(Modifier.weight(1f).fillMaxHeight()) {
            Canvas(Modifier.fillMaxSize()) {
                val a = Offset(size.width * 0.42f, 0f); val b = Offset(size.width * 0.62f, size.height)
                val br = Brush.linearGradient(listOf(Color(0xFFFF4D5E), Color(0xFFFFB347), Color(0xFF8E5CF7)), start = a, end = b)
                drawLine(br, a, b, strokeWidth = 40f, alpha = 0.12f, cap = StrokeCap.Round)
                drawLine(br, a, b, strokeWidth = 4f, cap = StrokeCap.Round)
            }
            content()
        }
    }
}

@Composable
private fun Sidebar(tab: Tab, onTab: (Tab) -> Unit) {
    val items: List<Pair<ImageVector, Tab?>> = listOf(
        Icons.Default.Search to null, Icons.Default.Home to Tab.Home, Icons.Default.Movie to Tab.Movies,
        Icons.Default.Theaters to Tab.Live, Icons.Default.Tv to Tab.Series, Icons.Default.Star to null
    )
    Box(Modifier.width(84.dp).fillMaxHeight().background(Color(0xFF0D0D0F))) {
        Column(Modifier.align(Alignment.Center), Arrangement.spacedBy(22.dp), Alignment.CenterHorizontally) {
            items.forEach { (ic, t) -> SideIcon(ic, t != null && t == tab) { if (t != null) onTab(t) } }
        }
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)) { SideIcon(Icons.Default.Settings, false) {} }
    }
}

@Composable
private fun SideIcon(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.size(52.dp).tvClickable(shape, onClick)
            .background(if (selected) Color(0x332F6BFF) else Color.Transparent)
            .then(if (selected) Modifier.border(2.dp, Color(0xFF4D6BFF), shape) else Modifier),
        Alignment.Center
    ) { Icon(icon, null, tint = if (selected) Color(0xFF6C8CFF) else Muted, modifier = Modifier.size(28.dp)) }
}
