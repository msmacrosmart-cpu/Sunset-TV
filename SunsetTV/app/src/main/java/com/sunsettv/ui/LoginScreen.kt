package com.sunsettv.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.CancellationException
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sunsettv.data.Library
import com.sunsettv.data.Repo
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoggedIn: (Library) -> Unit) {
    var url by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(28.dp)

    fun go() {
        if (url.isBlank() || user.isBlank() || pass.isBlank()) { err = "Preencha servidor, usuário e senha"; return }
        loading = true; err = null
        scope.launch {
            try { onLoggedIn(Repo.xtream(url, user.trim(), pass)) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { err = "Não foi possível entrar. Verifique URL, credenciais e conexão. " + if (e is IllegalArgumentException) e.message.orEmpty() else "Tente novamente." }
            loading = false
        }
    }

    Box(Modifier.fillMaxSize().background(Bg).imePadding().verticalScroll(rememberScrollState()).padding(16.dp), Alignment.Center) {
        Column(
            Modifier.widthIn(max = 440.dp).fillMaxWidth().clip(shape)
                .background(Brush.verticalGradient(listOf(Color(0x44FF4D5E), Color(0x14FFFFFF), Color(0x08FFFFFF))))
                .border(1.dp, Sunset, shape).padding(32.dp),
            Arrangement.spacedBy(14.dp), Alignment.CenterHorizontally
        ) {
            Text("Bem-vindo", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Medium)
            Text("Entre com sua conta Xtream Codes", color = Muted, fontSize = 12.sp)
            GlassField("URL (http://host:porta)", url, { url = it })
            GlassField("Usuário", user, { user = it })
            GlassField("Senha", pass, { pass = it }, password = true)
            err?.let { Text(it, color = Color(0xFFFF6B6B), fontSize = 12.sp) }
            Pill(if (loading) "Conectando..." else "Entrar", true, Modifier.fillMaxWidth()) { if (!loading) go() }
        }
    }
}

@Composable
fun Pill(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier.tvClickable(shape, onClick)
            .background(if (selected) Sunset else SolidColor(Color(0x22FFFFFF)))
            .padding(horizontal = 18.dp, vertical = 10.dp),
        Alignment.Center
    ) { Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun GlassField(label: String, value: String, onChange: (String) -> Unit, password: Boolean = false) {
    var f by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(50)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(Color(0x14FFFFFF))
            .border(if (f) 2.dp else 1.dp, if (f) Sunset else SolidColor(Color(0x33FFFFFF)), shape)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(label, color = Muted, fontSize = 11.sp)
        BasicTextField(
            value, onChange, singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
            cursorBrush = SolidColor(Color.White),
            visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
            modifier = Modifier.fillMaxWidth().onFocusChanged { f = it.isFocused }
        )
    }
}
