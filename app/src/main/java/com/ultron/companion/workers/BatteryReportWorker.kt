package com.ultron.companion.workers
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ultron.companion.UltronClient
import com.ultron.companion.system.BatteryMonitor
class BatteryReportWorker(c:Context,p:WorkerParameters):CoroutineWorker(c,p){
 override suspend fun doWork():Result{val b=BatteryMonitor(applicationContext).current();val ok=UltronClient.get(applicationContext).sendBatteryStatus(b.levelPercent,b.charging,b.temperatureC);return if(ok)Result.success() else Result.retry()}
}
