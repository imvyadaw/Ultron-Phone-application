package com.ultron.companion.ui.screens

import android.Manifest
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ultron.companion.EyesEarsService
import com.ultron.companion.HostStatus
import com.ultron.companion.LocationService
import com.ultron.companion.SecureStore
import com.ultron.companion.UltronClient
import com.ultron.companion.data.PreferencesManager

@Composable
fun SettingsScreen(
    client: UltronClient,
    state: String,
    hostStatus: HostStatus?,
    logs: List<String>,
    refreshStatus: () -> Unit,
    unpair: () -> Unit
) {
    val context = LocalContext.current
    val store = remember { SecureStore(context) }
    val prefs = remember { PreferencesManager(context) }
    var host by remember { mutableStateOf(store.pcHost) }
    var liveSenseOn by remember { mutableStateOf(false) }
    var locationOn by remember { mutableStateOf(false) }
    var clipboardSyncOn by remember { mutableStateOf(prefs.clipboardSyncEnabled) }
    var permissionMessage by remember { mutableStateOf("") }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val camera = grants[Manifest.permission.CAMERA] == true
        val mic = grants[Manifest.permission.RECORD_AUDIO] == true
        if (camera && mic) {
            context.startForegroundService(Intent(context, EyesEarsService::class.java))
            liveSenseOn = true
            permissionMessage = ""
        } else {
            permissionMessage = "Camera and microphone permissions are both required for Eyes & Ears."
        }
    }

    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            context.startForegroundService(Intent(context, LocationService::class.java))
            locationOn = true
            permissionMessage = ""
        } else {
            permissionMessage = "Location permission was not granted."
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Settings", style = MaterialTheme.typography.headlineSmall)
                Text("Connection: $state", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = refreshStatus) { Text("PC status") }
        }

        OutlinedTextField(
            value = host,
            onValueChange = { host = it; store.pcHost = it },
            label = { Text("PC IP / hostname") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        hostStatus?.let {
            Text("${it.hostName} • ${it.deviceCount} paired device(s)")
        }

        Text("Device ID: ${store.deviceId?.let { "stored securely" } ?: "not paired"}")
        Button(onClick = unpair) { Text("Unpair and stop sharing") }

        HorizontalDivider()
        Text("Eyes & Ears", style = MaterialTheme.typography.titleMedium)
        Text(
            if (liveSenseOn)
                "ON • camera frame about every 5s + microphone chunks + wake-word detection"
            else
                "OFF • ULTRON can only use phone camera/mic after you start this"
        )
        Button(onClick = {
            if (liveSenseOn) {
                context.stopService(Intent(context, EyesEarsService::class.java))
                liveSenseOn = false
            } else {
                permLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                )
            }
        }) { Text(if (liveSenseOn) "Stop Eyes & Ears" else "Start Eyes & Ears") }

        HorizontalDivider()
        Text("Location Sharing", style = MaterialTheme.typography.titleMedium)
        Text(if (locationOn) "ON • GPS updates about every 30s" else "OFF")
        Button(onClick = {
            if (locationOn) {
                context.stopService(Intent(context, LocationService::class.java))
                locationOn = false
            } else {
                locationPermLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }) { Text(if (locationOn) "Stop Location Sharing" else "Start Location Sharing") }

        HorizontalDivider()
        Text("Clipboard Sync", style = MaterialTheme.typography.titleMedium)
        Text(if (clipboardSyncOn) "ON • clipboard changes sent to ULTRON" else "OFF")
        Button(onClick = {
            clipboardSyncOn = !clipboardSyncOn
            prefs.clipboardSyncEnabled = clipboardSyncOn
            // If turning on while connected, start immediately; UltronClient
            // will also re-evaluate on next auth_ok (reconnect).
            if (clipboardSyncOn) client.applyClipboardSync() else client.stopClipboardSync()
        }) { Text(if (clipboardSyncOn) "Disable Clipboard Sync" else "Enable Clipboard Sync") }

        if (permissionMessage.isNotBlank()) {
            Text(permissionMessage, style = MaterialTheme.typography.bodySmall)
        }

        HorizontalDivider()
        Text("Logs", style = MaterialTheme.typography.titleMedium)
        LazyColumn(Modifier.weight(1f, fill = false)) {
            items(logs) { line -> Text(line, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
