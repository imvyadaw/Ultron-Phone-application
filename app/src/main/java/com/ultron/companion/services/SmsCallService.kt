package com.ultron.companion.services
import android.Manifest
import android.app.*;import android.content.*;import android.os.*;import androidx.core.app.NotificationCompat;import android.telephony.TelephonyManager;import androidx.core.content.ContextCompat;import com.ultron.companion.UltronClient
class SmsCallService:Service(){
 private var receiver:BroadcastReceiver?=null
 override fun onCreate(){super.onCreate();getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("ultron_telephony","ULTRON Telephony",NotificationManager.IMPORTANCE_LOW));startForeground(52,NotificationCompat.Builder(this,"ultron_telephony").setContentTitle("ULTRON Telephony Sync").setContentText("SMS and call events enabled").setSmallIcon(android.R.drawable.sym_action_call).setOngoing(true).build());receiver=object:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){when(i.action){TelephonyManager.ACTION_PHONE_STATE_CHANGED->UltronClient.get(c).sendCallState(i.getStringExtra(TelephonyManager.EXTRA_STATE).orEmpty())}}};registerReceiver(receiver,IntentFilter().apply{addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)},null,Handler(Looper.getMainLooper()))}
 override fun onDestroy(){receiver?.let{unregisterReceiver(it)};super.onDestroy()}
 override fun onBind(i:Intent?)=null
}
