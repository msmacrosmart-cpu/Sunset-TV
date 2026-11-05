package com.sunsettv.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

val Bg = Color(0xFF111113)
val CardBg = Color(0xFF1C1C20)
val Muted = Color(0xFF9A9AA3)
val Sunset = Brush.linearGradient(listOf(Color(0xFFFF4D5E), Color(0xFFFFB347), Color(0xFF8E5CF7)))

/** Foco D-Pad: escala + borda neon. Enter/OK no controle dispara onClick. */
fun Modifier.tvClickable(shape: Shape = RoundedCornerShape(12.dp), onClick: () -> Unit): Modifier = composed {
    var focused by remember { mutableStateOf(false) }
    val s by animateFloatAsState(if (focused) 1.06f else 1f, label = "focusScale")
    this.onFocusChanged { focused = it.isFocused }
        .scale(s)
        .then(if (focused) Modifier.border(3.dp, Sunset, shape) else Modifier)
        .clip(shape)
        .clickable(onClick = onClick)
}
