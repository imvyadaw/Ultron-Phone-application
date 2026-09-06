package com.ultron.companion.utils
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
object PermissionHelper {
 fun granted(context:Context,permission:String)=ContextCompat.checkSelfPermission(context,permission)==PackageManager.PERMISSION_GRANTED
 fun cameraAndMic(context:Context)=granted(context,Manifest.permission.CAMERA)&&granted(context,Manifest.permission.RECORD_AUDIO)
 fun location(context:Context)=granted(context,Manifest.permission.ACCESS_FINE_LOCATION)||granted(context,Manifest.permission.ACCESS_COARSE_LOCATION)
}
