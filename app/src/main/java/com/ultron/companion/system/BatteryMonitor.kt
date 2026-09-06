package com.ultron.companion.system
import android.content.*
import android.os.BatteryManager
import com.ultron.companion.data.models.BatteryStatus
class BatteryMonitor(private val context:Context) {
 fun current():BatteryStatus {
  val i=context.registerReceiver(null,IntentFilter(Intent.ACTION_BATTERY_CHANGED))
  val level=i?.getIntExtra(BatteryManager.EXTRA_LEVEL,-1)?:-1
  val scale=i?.getIntExtra(BatteryManager.EXTRA_SCALE,100)?:100
  val status=i?.getIntExtra(BatteryManager.EXTRA_STATUS,-1)?:-1
  val plugged=when(i?.getIntExtra(BatteryManager.EXTRA_PLUGGED,0)?:0){BatteryManager.BATTERY_PLUGGED_USB->"usb";BatteryManager.BATTERY_PLUGGED_AC->"ac";BatteryManager.BATTERY_PLUGGED_WIRELESS->"wireless";else->"none"}
  val temp=(i?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,Int.MIN_VALUE)?:Int.MIN_VALUE).takeIf{it!=Int.MIN_VALUE}?.div(10f)
  return BatteryStatus(if(scale>0) level*100/scale else -1,status==BatteryManager.BATTERY_STATUS_CHARGING||status==BatteryManager.BATTERY_STATUS_FULL,plugged,temp,i?.getStringExtra(BatteryManager.EXTRA_HEALTH)?:"unknown",System.currentTimeMillis())
 }
}
