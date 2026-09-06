package com.ultron.companion.system
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
object ScreenMirrorState {
 private val _jpeg=MutableStateFlow<ByteArray?>(null)
 val jpeg:StateFlow<ByteArray?>=_jpeg
 @Volatile var active:Boolean=false
 fun publish(bytes:ByteArray){_jpeg.value=bytes}
 fun clear(){_jpeg.value=null;active=false}
}
