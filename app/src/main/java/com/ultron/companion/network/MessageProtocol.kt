package com.ultron.companion.network

import org.json.JSONObject

sealed interface UltronMessage {
    fun json(): JSONObject
}

data class BatteryMessage(
    val level: Int,
    val charging: Boolean,
    val temperatureC: Float?,
    val timestampMs: Long
) : UltronMessage {
    override fun json(): JSONObject {
        val j = JSONObject()
            .put("type", "battery_status")
            .put("level", level)
            .put("charging", charging)
            .put("timestamp_ms", timestampMs)
        if (temperatureC != null) j.put("temperature_c", temperatureC.toDouble())
        return j
    }
}

data class NotificationMessage(
    val packageName: String,
    val title: String,
    val text: String,
    val key: String,
    val timestampMs: Long
) : UltronMessage {
    override fun json() = JSONObject()
        .put("type", "notification")
        .put("package_name", packageName)
        .put("title", title)
        .put("text", text)
        .put("key", key)
        .put("timestamp_ms", timestampMs)
}

data class SmsMessage(
    val sender: String,
    val body: String,
    val timestampMs: Long
) : UltronMessage {
    override fun json() = JSONObject()
        .put("type", "sms_received")
        .put("sender", sender)
        .put("body", body)
        .put("timestamp_ms", timestampMs)
}

data class ClipboardMessage(
    val text: String,
    val timestampMs: Long
) : UltronMessage {
    override fun json() = JSONObject()
        .put("type", "clipboard_update")
        .put("text", text)
        .put("timestamp_ms", timestampMs)
}

// Fixed: index and total are now proper data class properties (val)
data class FileChunkMessage(
    val transferId: String,
    val index: Int,
    val total: Int,
    val dataBase64: String
) : UltronMessage {
    override fun json() = JSONObject()
        .put("type", "file_chunk")
        .put("transfer_id", transferId)
        .put("index", index)
        .put("total", total)
        .put("data", dataBase64)
}
