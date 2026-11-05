package com.sunsettv.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onLogout: () -> Unit) {
    val prefs = LocalContext.current.getSharedPreferences("sunsettv", 0)
    var buffer by remember { mutableStateOf(prefs.getBoolean("large_buffer", false)) }
    var fit by remember { mutableStateOf(prefs.getBoolean("fill_video", false)) }
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Configurações", style = MaterialTheme.typography.headlineMedium)
        Text("Reprodução • aplicadas ao abrir o próximo vídeo")
        Row { Checkbox(buffer, { buffer = it; prefs.edit().putBoolean("large_buffer", it).apply() }); Text("Buffer ampliado para conexões instáveis", Modifier.padding(top = 12.dp).weight(1f)) }
        Row { Checkbox(fit, { fit = it; prefs.edit().putBoolean("fill_video", it).apply() }); Text("Preencher tela (pode cortar a imagem)", Modifier.padding(top = 12.dp).weight(1f)) }
        Text("No player: play/pausa, avanço e retrocesso, áudio, legendas, velocidade, proporção, tentar novamente e canais anterior/próximo. As faixas e qualidades disponíveis dependem do servidor.")
        Text("Controle remoto: use as setas e OK para navegar; Voltar fecha o player. Credenciais não são gravadas no aparelho.")
        Pill("Sair da conta", false) { confirm = true }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Sair da conta?") }, text = { Text("Será necessário informar URL, usuário e senha novamente.") }, confirmButton = { TextButton(onClick = onLogout) { Text("Sair") } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancelar") } })
}
