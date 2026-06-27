package com.example.smartnotes.recording

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRecognizerManager @Inject constructor() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var transcriptFile: File? = null

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText

    private val _finalText = MutableStateFlow("")
    val finalText: StateFlow<String> = _finalText

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private var fullTranscript = StringBuilder()

    fun startListening(context: Context, filePath: String) {
        if (SpeechRecognizer.isRecognitionAvailable(context).not()) return

        transcriptFile = File(filePath)
        transcriptFile?.parentFile?.mkdirs()

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("pt", "BR").toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { text ->
                    fullTranscript.append(" ").append(text)
                    _finalText.value = fullTranscript.toString()
                    saveTranscript()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { text ->
                    _partialText.value = text
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() { _isListening.value = true }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { _isListening.value = false }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
    }

    fun cancel() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _isListening.value = false
    }

    fun getFullTranscript(): String = fullTranscript.toString()

    private fun saveTranscript() {
        transcriptFile?.writeText(fullTranscript.toString())
    }

    fun loadTranscript(filePath: String): String {
        return try {
            File(filePath).readText()
        } catch (e: Exception) { "" }
    }
}
