package com.ultron.companion.system
import android.content.Context
import android.os.Build
import android.os.StatFs
import android.provider.Settings
import org.json.JSONObject
class DeviceInfoCollector(private val context:Context) {
 fun collect():JSONObject {
  val s=StatFs(context.filesDir.absolutePath)
  return JSONObject().put("manufacturer",Build.MANUFACTURER).put("model",Build.MODEL).put("android_version",Build.VERSION.RELEASE).put("sdk",Build.VERSION.SDK_INT).put("device",Build.DEVICE).put("app_id",context.packageName).put("free_bytes",s.availableBytes).put("total_bytes",s.totalBytes).put("android_id",Settings.Secure.getString(context.contentResolver,Settings.Secure.ANDROID_ID)?:"")
 }
}
