package com.ultron.companion.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import com.ultron.companion.services.ScreenMirrorService

object MediaProjectionHelper {

    fun createCaptureIntent(activity: Activity): Intent =
        activity.getSystemService(MediaProjectionManager::class.java)
            .createScreenCaptureIntent()

    fun createCaptureIntent(context: Context): Intent =
        context.getSystemService(MediaProjectionManager::class.java)
            .createScreenCaptureIntent()

    fun startServiceIntent(
        context: Context,
        resultCode: Int,
        data: Intent,
    ): Intent =
        Intent(context, ScreenMirrorService::class.java)
            .putExtra(EXTRA_RESULT_CODE, resultCode)
            .putExtra(EXTRA_DATA, data)

    const val EXTRA_RESULT_CODE = "result_code"
    const val EXTRA_DATA = "projection_data"
}
