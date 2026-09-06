package com.ultron.companion.ui.screens

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ultron.companion.services.ScreenMirrorService
import com.ultron.companion.system.ScreenMirrorState
import com.ultron.companion.utils.MediaProjectionHelper

@Composable
fun StreamingPreviewScreen() {
    val context = LocalContext.current
    val bytes by ScreenMirrorState.jpeg.collectAsState()
    var active by remember { mutableStateOf(ScreenMirrorState.active) }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            val data = result.data
            if (result.resultCode == android.app.Activity.RESULT_OK && data != null) {
                context.startForegroundService(
                    MediaProjectionHelper.startServiceIntent(
                        context = context,
                        resultCode = result.resultCode,
                        data = data,
                    ),
                )
                active = true
                ScreenMirrorState.active = true
            }
        }

    LaunchedEffect(bytes) {
        if (bytes != null) active = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Screen Mirror",
            style = MaterialTheme.typography.headlineSmall,
        )

        bytes?.let { jpeg ->
            BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size)?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "ULTRON screen preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        } ?: Text(
            if (active) "Waiting for the first frame…"
            else "Screen capture is stopped.",
        )

        Button(
            onClick = {
                if (active) {
                    context.stopService(Intent(context, ScreenMirrorService::class.java))
                    ScreenMirrorState.clear()
                    active = false
                } else {
                    launcher.launch(MediaProjectionHelper.createCaptureIntent(context))
                }
            },
        ) {
            Text(if (active) "Stop Screen Mirror" else "Start Screen Mirror")
        }
    }
}
