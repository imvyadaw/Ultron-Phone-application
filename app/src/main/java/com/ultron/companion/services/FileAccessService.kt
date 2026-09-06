package com.ultron.companion.services
import android.app.*;import android.content.*;import android.net.Uri;import androidx.core.app.NotificationCompat;import com.ultron.companion.UltronClient;import java.io.InputStream;import java.util.UUID
class FileAccessService:Service(){
 override fun onStartCommand(i:Intent?,f:Int,s:Int):Int{if(i?.action=="SEND_URI"){val uri=i.data?:return START_NOT_STICKY;send(uri)};return START_NOT_STICKY}
 private fun send(uri:Uri){Thread{runCatching{contentResolver.openInputStream(uri)?.use{stream->val id=UUID.randomUUID().toString();val total=(contentResolver.openAssetFileDescriptor(uri,"r")?.length?:-1).coerceAtLeast(-1);val buf=ByteArray(48*1024);var idx=0;var n:Int;while(stream.read(buf).also{n=it}>0){UltronClient.get(applicationContext).sendFileChunk(id,idx++,if(total>0)((total+buf.size-1)/buf.size).toInt() else -1,buf.copyOf(n))}}}}.onFailure{android.util.Log.e("FileAccessService","transfer failed",it)}}.start()}
 override fun onCreate(){super.onCreate();getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("ultron_files","ULTRON Files",NotificationManager.IMPORTANCE_LOW));startForeground(53,NotificationCompat.Builder(this,"ultron_files").setContentTitle("ULTRON File Access").setContentText("User-selected transfers").setSmallIcon(android.R.drawable.ic_menu_upload).build())}
 override fun onBind(i:Intent?)=null
}
