package com.example.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class VoiceAssistantHandler(
    private val context: Context,
    private val onTranscriptUpdate: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit,
    private val onError: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        if (!isAvailable) {
            onError("Speech recognition not available on this device. You can type or tap a sample rant below.")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onListeningStateChanged(true)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    onListeningStateChanged(false)
                }

                override fun onError(error: Int) {
                    onListeningStateChanged(false)
                    val message = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
                        SpeechRecognizer.ERROR_NETWORK -> "Network required for speech-to-text"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Try again or tap sample prompt."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Tap to speak again."
                        else -> "Speech recognition paused ($error)"
                    }
                    onError(message)
                }

                override fun onResults(results: Bundle?) {
                    onListeningStateChanged(false)
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        onTranscriptUpdate(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()
                    if (!text.isNullOrBlank()) {
                        onTranscriptUpdate(text)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            onListeningStateChanged(false)
            onError("Failed to start voice capture: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        onListeningStateChanged(false)
    }

    companion object {
        val SAMPLE_RANTS = listOf(
            "I have to finish my job assignment by Friday, prepare for an interview, pay my electricity bill, and study Java for an hour every day.",
            "Schedule dentist checkup tomorrow morning, review Q3 team budget by 3pm, and go for a 5km run before dinner.",
            "Call landlord about the lease extension, buy groceries for weekly meal prep, and read 2 chapters of Designing Data-Intensive Applications."
        )
    }
}
