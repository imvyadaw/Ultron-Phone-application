package com.ultron.companion.system

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

class WakeWordDetector(
    private val context: Context,
    private val wakeWord: String = "ultron",
    private val onDetected: () -> Unit,
) {
    private var recognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(
                object : RecognitionListener {
                    override fun onResults(results: Bundle) {
                        val alternatives =
                            results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).orEmpty()

                        if (alternatives.any { it.contains(wakeWord, ignoreCase = true) }) {
                            onDetected()
                        }
                        restart()
                    }

                    override fun onError(error: Int) {
                        handler.postDelayed({ startListening() }, RESTART_DELAY_MS)
                    }

                    override fun onReadyForSpeech(params: Bundle?) = Unit
                    override fun onBeginningOfSpeech() = Unit
                    override fun onRmsChanged(rmsdB: Float) = Unit
                    override fun onBufferReceived(buffer: ByteArray?) = Unit
                    override fun onEndOfSpeech() = Unit
                    override fun onPartialResults(partialResults: Bundle?) = Unit
                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                },
            )
        }

        startListening()
    }

    private fun startListening() {
        val currentRecognizer = recognizer ?: return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        currentRecognizer.startListening(intent)
    }

    private fun restart() {
        handler.post { startListening() }
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
        recognizer?.destroy()
        recognizer = null
    }

    companion object {
        private const val RESTART_DELAY_MS = 1_000L
    }
}
