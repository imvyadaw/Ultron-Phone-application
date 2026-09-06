package com.ultron.companion.system
import android.content.*
class ClipboardSync(private val context:Context,private val onChange:(String)->Unit) {
 private val manager=context.getSystemService(ClipboardManager::class.java)
 private var last:String?=null
 private val listener=ClipboardManager.OnPrimaryClipChangedListener {
  val text=manager.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
  if(text.isNotBlank()&&text!=last){last=text;onChange(text)}
 }
 fun start(){manager.addPrimaryClipChangedListener(listener)}
 fun stop(){manager.removePrimaryClipChangedListener(listener)}
}
