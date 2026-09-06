package com.ultron.companion.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ultron.companion.UltronClient
import java.util.UUID
import kotlin.concurrent.thread

class FileAccessService : Service() {

    override fun onCreate() {
        super.onCreate()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "ULTRON Files",
                NotificationManager.IMPORTANCE_LOW,
            ),
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ULTRON File Access")
            .setContentText("User-selected transfers")
            .setSmallIcon(android.R.drawable.ic_menu_upload)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SEND_URI) {
            val uri = intent.data
            if (uri != null) {
                send(uri)
            } else {
                Log.w(TAG, "SEND_URI requested without a URI")
            }
        }
        return START_NOT_STICKY
    }

    private fun send(uri: Uri) {
        thread(
            name = "ultron-file-transfer",
            start = true,
        ) {
            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    val id = UUID.randomUUID().toString()
                    val total = contentResolver
                        .openAssetFileDescriptor(uri, "r")
                        ?.use { descriptor -> descriptor.length }
                        ?: -1L

                    val chunkSize = 48 * 1024
                    val totalChunks =
                        if (total > 0L) ((total + chunkSize - 1L) / chunkSize).toInt() else -1

                    val buffer = ByteArray(chunkSize)
                    var index = 0

                    while (true) {
                        val count = stream.read(buffer)
                        if (count <= 0) break

                        UltronClient.get(applicationContext).sendFileChunk(
                            id = id,
                            index = index++,
                            total = totalChunks,
                            data = buffer.copyOf(count),
                        )
                    }
                } ?: Log.e(TAG, "Unable to open selected URI: $uri")
            } catch (error: Exception) {
                Log.e(TAG, "File transfer failed for URI: $uri", error)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "FileAccessService"
        private const val CHANNEL_ID = "ultron_files"
        private const val NOTIFICATION_ID = 53
        const val ACTION_SEND_URI = "SEND_URI"
    }
}
