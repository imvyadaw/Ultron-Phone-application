package com.ultron.companion.network

import android.util.Base64
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Bounded, backpressure-aware frame sender.
 * [sender] receives a complete JSON string and returns true if sent.
 * When the queue is full the oldest pending frame is dropped so new
 * frames are never blocked (screen mirror / video are loss-tolerant).
 */
class StreamManager(private val sender: (String) -> Boolean, capacity: Int = 4) {
    private val queue = ArrayBlockingQueue<String>(capacity)
    private val running = AtomicBoolean(true)
    private val worker = Thread({
        while (running.get()) {
            try {
                val payload = queue.take()
                if (!sender(payload)) Thread.sleep(250)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
        }
    }, "ultron-stream-sender").apply { isDaemon = true; start() }

    /**
     * Enqueue a frame. [extras] are merged into the JSON object alongside
     * "type" and "data" (e.g. mapOf("format" to "jpeg")).
     * Returns true if the frame was queued; false if it was dropped.
     */
    fun offer(type: String, data: ByteArray, extras: Map<String, String> = emptyMap()): Boolean {
        if (!running.get()) return false
        val json = org.json.JSONObject()
            .put("type", type)
            .put("data", Base64.encodeToString(data, Base64.NO_WRAP))
        extras.forEach { (k, v) -> json.put(k, v) }
        val msg = json.toString()
        if (queue.offer(msg)) return true
        queue.poll()          // drop oldest frame
        return queue.offer(msg)
    }

    fun close() {
        running.set(false)
        worker.interrupt()
        queue.clear()
    }
}
