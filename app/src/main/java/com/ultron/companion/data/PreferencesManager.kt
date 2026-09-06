package com.ultron.companion.data
import android.content.Context
class PreferencesManager(context:Context) {
 private val p=context.applicationContext.getSharedPreferences("ultron_preferences",Context.MODE_PRIVATE)
 var notificationsEnabled:Boolean get()=p.getBoolean("notifications_enabled",false); set(v){p.edit().putBoolean("notifications_enabled",v).apply()}
 var clipboardSyncEnabled:Boolean get()=p.getBoolean("clipboard_sync_enabled",false); set(v){p.edit().putBoolean("clipboard_sync_enabled",v).apply()}
 var batteryReportsEnabled:Boolean get()=p.getBoolean("battery_reports_enabled",true); set(v){p.edit().putBoolean("battery_reports_enabled",v).apply()}
 var lastBatteryReportMs:Long get()=p.getLong("last_battery_report_ms",0); set(v){p.edit().putLong("last_battery_report_ms",v).apply()}
}
