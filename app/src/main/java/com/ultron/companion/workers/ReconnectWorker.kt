package com.ultron.companion.workers
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ultron.companion.UltronClient
class ReconnectWorker(c:Context,p:WorkerParameters):CoroutineWorker(c,p){
 override suspend fun doWork():Result{val c=UltronClient.get(applicationContext);if(!c.isConnected())c.connect();return Result.success()}
}
