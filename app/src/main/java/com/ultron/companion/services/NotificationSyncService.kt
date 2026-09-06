package com.ultron.companion.services
import android.app.Notification;import android.service.notification.NotificationListenerService;import android.service.notification.StatusBarNotification;import com.ultron.companion.UltronClient
class NotificationSyncService:NotificationListenerService(){
 override fun onNotificationPosted(s:StatusBarNotification){val n=s.notification;val e=n.extras;UltronClient.get(applicationContext).sendNotification(s.packageName,e?.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty(),e?.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty(),s.key,s.postTime)}
 override fun onNotificationRemoved(s:StatusBarNotification){ }
}
