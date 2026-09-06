package com.ultron.companion.services

import android.app.*
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.ultron.companion.UltronClient
import com.ultron.companion.network.StreamManager
import java.io.ByteArrayOutputStream
import kotlin.math.min

class ScreenMirrorService : Service() {
    companion object {
        const val ACTION_STOP = "com.ultron.companion.STOP_SCREEN"
        private const val CH = "ultron_screen"
        private const val ID = 51
    }

    private lateinit var projection: MediaProjection
    private var reader: ImageReader? = null
    private var virtual: android.hardware.display.VirtualDisplay? = null
    private var handler: Handler? = null
    private var stream: StreamManager? = null

    override fun onCreate() {
        super.onCreate()
        com.ultron.companion.system.ScreenMirrorState.active = true
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CH, "ULTRON Screen Mirror", NotificationManager.IMPORTANCE_LOW)
        )
        startForeground(
            ID, NotificationCompat.Builder(this, CH)
                .setContentTitle("ULTRON Screen Mirror")
                .setContentText("Screen sharing active")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setOngoing(true).build()
        )
        // StreamManager throttles frame delivery — drops oldest frame when
        // the queue is full instead of flooding a slow connection.
        stream = StreamManager(
            sender = { json -> UltronClient.get(applicationContext).sendRaw(json) },
            capacity = 4
        )
    }

    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        if (i?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        val code = i?.getIntExtra(com.ultron.companion.utils.MediaProjectionHelper.EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
            ?: Activity.RESULT_CANCELED
        val data = i?.getParcelableExtra<Intent>(com.ultron.companion.utils.MediaProjectionHelper.EXTRA_DATA)
        if (code == Activity.RESULT_OK && data != null) startCapture(code, data) else stopSelf()
        return START_NOT_STICKY
    }

    private fun startCapture(code: Int, data: Intent) {
        val mgr = getSystemService(MediaProjectionManager::class.java)
        projection = mgr.getMediaProjection(code, data)
        val dm = resources.displayMetrics
        val w = (dm.widthPixels / 2).coerceAtLeast(320)
        val h = (dm.heightPixels / 2).coerceAtLeast(240)
        reader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)
        handler = Handler(Looper.getMainLooper())
        reader!!.setOnImageAvailableListener({ r ->
            r.acquireLatestImage()?.use { img ->
                val plane = img.planes[0]
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val buffer = plane.buffer
                val rowStride = plane.rowStride
                val pixelStride = plane.pixelStride
                val row = ByteArray(rowStride)
                for (y in 0 until h) {
                    buffer.position(y * rowStride)
                    buffer.get(row, 0, minOf(row.size, buffer.remaining()))
                    for (x in 0 until w) {
                        val o = x * pixelStride
                        if (o + 3 < row.size)
                            bmp.setPixel(x, y, android.graphics.Color.argb(
                                row[o + 3].toInt() and 255,
                                row[o + 0].toInt() and 255,
                                row[o + 1].toInt() and 255,
                                row[o + 2].toInt() and 255
                            ))
                    }
                }
                val out = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 65, out)
                bmp.recycle()
                val bytes = out.toByteArray()
                // Publish to UI preview state
                com.ultron.companion.system.ScreenMirrorState.publish(bytes)
                // Route through StreamManager (backpressure + rate limiting)
                stream?.offer("screen_frame", bytes, mapOf("format" to "jpeg"))
            }
        }, handler)
        virtual = projection.createVirtualDisplay(
            "ULTRON", w, h, dm.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            reader!!.surface, null, null
        )
        projection.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { stopSelf() }
        }, handler)
    }

    override fun onDestroy() {
        com.ultron.companion.system.ScreenMirrorState.clear()
        stream?.close()
        stream = null
        reader?.close()
        virtual?.release()
        if (::projection.isInitialized) projection.stop()
        handler?.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(i: Intent?) = null
}
