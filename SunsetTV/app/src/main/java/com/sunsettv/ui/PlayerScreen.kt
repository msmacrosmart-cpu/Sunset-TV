package com.sunsettv.ui

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.sunsettv.data.Channel

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun PlayerScreen(ch: Channel, onClose: () -> Unit, onPrevious: (() -> Unit)? = null, onNext: (() -> Unit)? = null) {
    val ctx = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val prefs = remember { ctx.getSharedPreferences("sunsettv", 0) }
    var error by remember(ch.url) { mutableStateOf(false) }
    var controls by remember { mutableStateOf(true) }
    var fill by remember { mutableStateOf(prefs.getBoolean("fill_video", false)) }
    val player = remember(ch.url) {
        val buffer = if (prefs.getBoolean("large_buffer", false)) 60000 else 30000
        ExoPlayer.Builder(ctx).setLoadControl(DefaultLoadControl.Builder().setBufferDurationsMs(buffer, buffer * 2, 1500, 3000).build()).setSeekBackIncrementMs(10000).setSeekForwardIncrementMs(10000).build().apply {
            setMediaItem(MediaItem.fromUri(ch.url)); prepare(); playWhenReady = true
        }
    }
    DisposableEffect(player, owner) {
        val listener = object : Player.Listener {
            override fun onPlayerError(e: PlaybackException) { error = true; controls = true }
        }
        var resume = false
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) { resume = player.playWhenReady; player.pause() }
            if (event == Lifecycle.Event.ON_RESUME && resume) player.play()
        }
        player.addListener(listener); owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); player.removeListener(listener); player.release() }
    }
    Column(Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding()) {
        if (controls || error) {
            Text(ch.name, color = Color.White, modifier = Modifier.padding(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(8.dp)) {
                item { Pill("Voltar", false, onClick = onClose) }
                if (onPrevious != null) item { Pill("Canal −", false, onClick = onPrevious) }
                if (onNext != null) item { Pill("Canal +", false, onClick = onNext) }
                item { Pill(if (fill) "Ajustar imagem" else "Preencher tela", false) { fill = !fill } }
                item { Pill("Tentar novamente", false) { error = false; player.prepare(); player.play() } }
            }
        }
        if (error) Text("Não foi possível reproduzir. Verifique a conexão, o limite de acessos da conta ou tente outro canal.", color = Color.White, modifier = Modifier.padding(12.dp))
        AndroidView(factory = { context ->
            PlayerView(context).apply {
                this.player = player
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                setShowSubtitleButton(true)
                setShowFastForwardButton(true); setShowRewindButton(true)
                setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { controls = it == android.view.View.VISIBLE })
                keepScreenOn = true
                isFocusable = true; requestFocus()
                setOnKeyListener { _, code, event ->
                    if (event.action != KeyEvent.ACTION_DOWN) false else when (code) {
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> { if (player.isPlaying) player.pause() else player.play(); true }
                        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> { player.seekForward(); true }
                        KeyEvent.KEYCODE_MEDIA_REWIND -> { player.seekBack(); true }
                        else -> false
                    }
                }
            }
        }, update = { it.player = player; it.resizeMode = if (fill) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT }, modifier = Modifier.fillMaxWidth().weight(1f))
    }
}
