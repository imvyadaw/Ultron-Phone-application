package com.ultron.companion.services
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.ultron.companion.UltronClient
class SmsReceiver:BroadcastReceiver(){
 override fun onReceive(context:Context,intent:Intent){
  if(intent.action!=Telephony.Sms.Intents.SMS_RECEIVED_ACTION)return
  for(s in Telephony.Sms.Intents.getMessagesFromIntent(intent)) UltronClient.get(context).sendSms(s.originatingAddress.orEmpty(),s.messageBody.orEmpty())
 }
}
