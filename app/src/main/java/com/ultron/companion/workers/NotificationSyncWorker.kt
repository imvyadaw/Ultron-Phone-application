package com.ultron.companion.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ultron.companion.UltronClient
import kotlinx.coroutines.delay

/**
 * Periodic worker that ensures the WebSocket connection is alive so that
 * NotificationSyncService (a NotificationListenerService) can forward
 * notifications to ULTRON without interruption.
 */
class NotificationSyncWorker(c: Context, p: WorkerParameters) : CoroutineWorker(c, p) {
    override suspend fun doWork(): Result {
        val client = UltronClient.get(applicationContext)
        if (!client.isConnected()) {
            client.connect()
            delay(3_000)   // give the WebSocket time to authenticate
        }
        return if (client.isConnected()) Result.success() else Result.retry()
    }
}
