package com.ultron.companion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ultron.companion.ui.StatusDot
import com.ultron.companion.ui.UltronBrandHeader
import com.ultron.companion.ui.UltronMark
import com.ultron.companion.ui.UltronTheme
import com.ultron.companion.ui.screens.SettingsScreen
import com.ultron.companion.ui.screens.StreamingPreviewScreen
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
        }
        setContent { App(UltronClient.get(applicationContext)) }
    }
}

data class ChatLine(
    val fromUser: Boolean,
    val text: String,
    val imageUrl: String? = null,
    val imageTitle: String? = null,
    val videoUrl: String? = null,
    val videoTitle: String? = null
)

@Composable
fun App(client: UltronClient) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var paired by remember { mutableStateOf(SecureStore(context).deviceId != null) }
    var state by remember { mutableStateOf("disconnected") }
    var commands by remember { mutableStateOf(emptyList<String>()) }
    var last by remember { mutableStateOf<CommandResult?>(null) }
    var hostStatus by remember { mutableStateOf<HostStatus?>(null) }
    val logs = remember { mutableStateListOf<String>() }
    val chatLines = remember { mutableStateListOf<ChatLine>() }
    var chatPending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                runCatching { tts.value?.language = Locale.getDefault() }
            }
        }
        tts.value = engine
        onDispose { engine.stop(); engine.shutdown() }
    }

    DisposableEffect(client) {
        val stateListener: (String) -> Unit = { state = it }
        val resultListener: (CommandResult) -> Unit = { last = it }
        val logListener: (String) -> Unit = { msg ->
            if (logs.size >= 200) logs.removeFirst()
            logs.add(msg)
        }
        val chatListener: (ChatReply) -> Unit = { r ->
            chatPending = false
            if (r.ok) {
                chatLines.add(
                    ChatLine(
                        fromUser = false,
                        text = r.text,
                        imageUrl = r.imageUrl.ifBlank { null },
                        imageTitle = r.imageTitle.ifBlank { null },
                        videoUrl = r.videoUrl.ifBlank { null },
                        videoTitle = r.videoTitle.ifBlank { null }
                    )
                )
                val speech = r.spokenText.ifBlank { r.text }
                if (speech.isNotBlank()) {
                    tts.value?.speak(speech, TextToSpeech.QUEUE_FLUSH, null, "ultron_reply")
                }
            } else {
                chatLines.add(ChatLine(false, "(error) ${r.error.ifBlank { "ULTRON did not reply" }}"))
            }
        }
        client.addStateListener(stateListener)
        client.addResultListener(resultListener)
        client.addLogListener(logListener)
        client.addChatListener(chatListener)
        onDispose {
            client.removeStateListener(stateListener)
            client.removeResultListener(resultListener)
            client.removeLogListener(logListener)
            client.removeChatListener(chatListener)
        }
    }

    fun refreshCommands() {
        scope.launch {
            client.commands().onSuccess { commands = it }
                .onFailure { logs.add("Command catalog refresh failed: ${it.message.orEmpty()}") }
        }
    }

    LaunchedEffect(paired) {
        if (paired) {
            client.connect()
            refreshCommands()
        }
    }

    UltronTheme {
        val tabIcons = listOf(Icons.Filled.Link, Icons.AutoMirrored.Filled.Chat, Icons.Filled.Terminal, Icons.Filled.Settings, Icons.Filled.PlayArrow)
        val tabTitles = listOf("Pair", "Chat", "Commands", "Settings", "Mirror")
        Scaffold(
            topBar = {
                Surface(shadowElevation = 2.dp, color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        UltronBrandHeader(
                            subtitle = tabTitles[tab],
                            connected = state == "connected"
                        )
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabTitles.forEachIndexed { i, title ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(tabIcons[i], contentDescription = title) },
                            label = { Text(title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding).padding(16.dp)) {
                when (tab) {
                    0 -> PairScreen(client) {
                        paired = true
                        tab = 1
                        ContextCompat.startForegroundService(
                            context, Intent(context, UltronConnectionService::class.java)
                        )
                    }
                    1 -> ChatScreen(
                        state = state,
                        lines = chatLines,
                        pending = chatPending
                    ) { text ->
                        if (text.isNotBlank()) {
                            chatLines.add(ChatLine(true, text))
                            chatPending = client.sendChatMessage(text)
                            if (!chatPending) chatPending = false
                        }
                    }
                    2 -> CommandScreen(
                        commands = commands,
                        state = state,
                        last = last,
                        refresh = ::refreshCommands
                    ) { name, args ->
                        if (!client.sendCommand(name, args)) {
                            last = CommandResult(name, false, "ULTRON is not connected")
                        }
                    }
                    3 -> SettingsScreen(
                        client = client,
                        state = state,
                        hostStatus = hostStatus,
                        logs = logs,
                        refreshStatus = {
                            scope.launch {
                                client.hostStatus()
                                    .onSuccess { hostStatus = it }
                                    .onFailure { logs.add("Host status failed: ${it.message.orEmpty()}") }
                            }
                        }
                    ) {
                        context.stopService(Intent(context, EyesEarsService::class.java))
                        context.stopService(Intent(context, LocationService::class.java))
                        client.close()
                        SecureStore(context).clearPair()
                        paired = false
                        commands = emptyList()
                        hostStatus = null
                        tab = 0
                    }
                    4 -> StreamingPreviewScreen()
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    state: String,
    lines: List<ChatLine>,
    pending: Boolean,
    send: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) scope.launch {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        val spoken = res.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!spoken.isNullOrBlank()) send(spoken)
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                .putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask ULTRON…")
            runCatching { speechLauncher.launch(intent) }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Text("Chat with ULTRON", style = MaterialTheme.typography.headlineSmall)
        Text("Connection: $state", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))

        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
            items(lines) { line ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (line.fromUser) Arrangement.End else Arrangement.Start
                ) {
                    Column(horizontalAlignment = if (line.fromUser) Alignment.End else Alignment.Start) {
                        Surface(
                            color = if (line.fromUser) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (line.fromUser) Color.White
                                    else MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(
                                topStart = 14.dp, topEnd = 14.dp,
                                bottomStart = if (line.fromUser) 14.dp else 2.dp,
                                bottomEnd = if (line.fromUser) 2.dp else 14.dp
                            ),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(line.text, Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                        }
                        // Real photo ULTRON actually found for this turn
                        // (show_image tool) - shown inline, tap to view
                        // full-size in the phone's own photo/browser app.
                        if (!line.imageUrl.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            val ctx = androidx.compose.ui.platform.LocalContext.current
                            coil.compose.AsyncImage(
                                model = line.imageUrl,
                                contentDescription = line.imageTitle ?: "Photo",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier
                                    .widthIn(max = 220.dp)
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        runCatching {
                                            ctx.startActivity(
                                                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(line.imageUrl))
                                            )
                                        }
                                    }
                            )
                        }
                        // Video ULTRON resolved (movie_assistant) - a
                        // tappable card instead of trying to embed a
                        // player; opens the phone's own YouTube app or
                        // browser via ACTION_VIEW.
                        if (!line.videoUrl.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            val ctx = androidx.compose.ui.platform.LocalContext.current
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .widthIn(max = 220.dp)
                                    .clickable {
                                        runCatching {
                                            ctx.startActivity(
                                                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(line.videoUrl))
                                            )
                                        }
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play")
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        line.videoTitle?.ifBlank { "Play video" } ?: "Play video",
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (pending) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        StatusDot(connected = true)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "ULTRON is thinking…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Type a message…") },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(6.dp))
            FilledIconButton(
                onClick = { micPermLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) { Icon(Icons.Filled.Mic, contentDescription = "Voice input") }
            Spacer(Modifier.width(6.dp))
            FilledIconButton(
                onClick = {
                    val text = input.trim()
                    if (text.isNotEmpty()) {
                        send(text)
                        input = ""
                    }
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send") }
        }
    }
}

@Composable
fun PairScreen(client: UltronClient, onPaired: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val store = remember { SecureStore(context) }
    var host by remember { mutableStateOf(store.pcHost) }
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf(Build.MODEL) }
    var msg by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                UltronMark(size = 56.dp)
                Spacer(Modifier.height(10.dp))
                Text("Pair with ULTRON", style = MaterialTheme.typography.headlineSmall)
            }
        }
        Text(
            "Enter the PC LAN/Tailscale address and the 6-digit code shown by ULTRON.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("PC IP / hostname") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter(Char::isDigit).take(6) },
            label = { Text("6-digit pairing code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Phone name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            enabled = !busy && host.isNotBlank() && code.length == 6,
            onClick = {
                busy = true
                scope.launch {
                    val r = client.pair(host, code, name)
                    busy = false
                    r.onSuccess {
                        msg = "Paired successfully."
                        onPaired()
                    }.onFailure {
                        msg = it.message ?: "Pairing failed."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy) "Pairing…" else "Pair") }

        if (msg.isNotBlank()) Text(msg)
    }
}

private fun org.json.JSONObject.toStringMap(): Map<String, Any?> =
    keys().asSequence().associateWith { key ->
        when (val v = get(key)) {
            org.json.JSONObject.NULL -> null
            is org.json.JSONObject -> v.toStringMap()
            is org.json.JSONArray -> v.toStringList()
            else -> v
        }
    }

private fun org.json.JSONArray.toStringList(): List<Any?> =
    (0 until length()).map { i ->
        when (val v = get(i)) {
            org.json.JSONObject.NULL -> null
            is org.json.JSONObject -> v.toStringMap()
            is org.json.JSONArray -> v.toStringList()
            else -> v
        }
    }

@Composable
fun CommandScreen(
    commands: List<String>,
    state: String,
    last: CommandResult?,
    refresh: () -> Unit,
    send: (String, Map<String, Any?>) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var argsText by remember { mutableStateOf("{}") }
    var selected by remember { mutableStateOf<String?>(null) }

    val filtered = remember(commands, query) {
        if (query.isBlank()) commands
        else commands.filter { it.contains(query, ignoreCase = true) }
    }

    fun parseArgs(): Map<String, Any?>? = runCatching {
        val j = org.json.JSONObject(argsText.ifBlank { "{}" })
        j.keys().asSequence().associateWith { key ->
            when (val value = j.get(key)) {
                org.json.JSONObject.NULL -> null
                is org.json.JSONObject -> value.toStringMap()
                is org.json.JSONArray -> value.toStringList()
                else -> value
            }
        }
    }.getOrNull()

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Commands", style = MaterialTheme.typography.headlineSmall)
                Text("Connection: $state • ${commands.size} tools")
            }
            TextButton(onClick = refresh) { Text("Refresh") }
        }

        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search tools…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))
        if (selected != null) {
            Text("Selected: $selected", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = argsText,
                onValueChange = { argsText = it },
                label = { Text("Args JSON (e.g. {\"path\":\"C:/x\"})") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    parseArgs()?.let { send(selected!!, it) }
                }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Run")
                }
                OutlinedButton(onClick = {
                    selected = null
                    argsText = "{}"
                }) { Text("Cancel") }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (commands.isEmpty()) {
            Text("No tools available. Check pairing, PC address, and ULTRON connection.")
        }

        LazyColumn(Modifier.weight(1f, fill = false)) {
            items(filtered) { command ->
                val isSelected = command == selected
                Card(
                    onClick = { selected = command; argsText = "{}" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                         else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Terminal,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 14.dp).size(18.dp)
                        )
                        Text(command, Modifier.padding(14.dp))
                    }
                }
            }
        }

        last?.let {
            Spacer(Modifier.height(8.dp))
            Text("Result: ${it.command} — ${if (it.ok) "OK" else "FAILED"}")
            if (it.detail.isNotBlank()) {
                Text(it.detail, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
