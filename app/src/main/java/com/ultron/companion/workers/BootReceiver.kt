package com.ultron.companion.workers
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
class BootReceiver:BroadcastReceiver(){
 override fun onReceive(context:Context,intent:Intent){if(intent.action!=Intent.ACTION_BOOT_COMPLETED)return
  WorkManager.getInstance(context).enqueueUniquePeriodicWork("ultron-reconnect",ExistingPeriodicWorkPolicy.UPDATE,PeriodicWorkRequestBuilder<ReconnectWorker>(15,TimeUnit.MINUTES).build())
 }
}
