package com.ultron.companion.system
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
class WakeWordDetector(private val context:Context,private val wakeWord:String="ultron",private val onDetected:()->Unit) {
 private var recognizer:SpeechRecognizer?=null
 private val handler=Handler(Looper.getMainLooper())
 fun start() {
  if(!SpeechRecognizer.isRecognitionAvailable(context)) return
  recognizer=SpeechRecognizer.createSpeechRecognizer(context).apply {
   setRecognitionListener(object:RecognitionListener{
    override fun onResults(results:android.os.Bundle){val a=results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty();if(a.any{it.contains(wakeWord,true)})onDetected();restart()}
    override fun onError(error:Int){handler.postDelayed({startListening()},1000)}
    override fun onReadyForSpeech(p:android.os.Bundle?){}
    override fun onBeginningOfSpeech(){}
    override fun onRmsChanged(v:Float){}
    override fun onBufferReceived(b:ByteArray?){}
    override fun onEndOfSpeech(){}
    override fun onPartialResults(b:android.os.Bundle?){}
    override fun onEvent(t:Int,b:android.os.Bundle?){}
   })
  };startListening()
 }
 private fun startListening(){recognizer?.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false)})}
 private fun restart(){handler.post{startListening()}}
 fun stop(){handler.removeCallbacksAndMessages(null);recognizer?.destroy();recognizer=null}
}
