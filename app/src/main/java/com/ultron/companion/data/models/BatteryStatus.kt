package com.ultron.companion.data.models
data class BatteryStatus(val levelPercent:Int,val charging:Boolean,val plugged:String,val temperatureC:Float?,val health:String,val timestampMs:Long)
