package com.ultron.companion.utils
import android.Manifest
import android.content.Context
object PhoneCallHelper {
 fun canReadCallState(context:Context)=PermissionHelper.granted(context,Manifest.permission.READ_PHONE_STATE)
 fun canReadSms(context:Context)=PermissionHelper.granted(context,Manifest.permission.READ_SMS)
}
