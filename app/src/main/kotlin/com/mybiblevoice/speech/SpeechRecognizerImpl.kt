package com.mybiblevoice.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer as AndroidSpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** No background/always-on listening: a recognizer session only exists between
 *  [startListening] and its terminal result/error (SRS section 12). No audio is persisted. */
class SpeechRecognizerImpl(private val context: Context) : SpeechRecognizer {

    private val _state = MutableStateFlow<SpeechState>(SpeechState.Idle)
    override val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var recognizer: AndroidSpeechRecognizer? = null

    override fun startListening(languageTag: String) {
        if (!AndroidSpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = SpeechState.Error("Speech recognition is not available on this device.")
            return
        }

        recognizer?.destroy()
        val newRecognizer = AndroidSpeechRecognizer.createSpeechRecognizer(context)
        recognizer = newRecognizer
        newRecognizer.setRecognitionListener(listener)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
        }
        _state.value = SpeechState.Listening
        newRecognizer.startListening(intent)
    }

    override fun stopListening() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
        _state.value = SpeechState.Idle
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = SpeechState.Listening
        }

        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            _state.value = SpeechState.Processing
        }

        override fun onError(error: Int) {
            _state.value = SpeechState.Error(describeError(error))
        }

        override fun onResults(results: Bundle?) {
            val text = results
                ?.getStringArrayList(AndroidSpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
            _state.value = if (text.isNullOrBlank()) {
                SpeechState.Error("No speech was recognized.")
            } else {
                SpeechState.Success(text)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun describeError(error: Int): String = when (error) {
        AndroidSpeechRecognizer.ERROR_NO_MATCH -> "No speech was recognized."
        AndroidSpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech was detected."
        AndroidSpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required."
        AndroidSpeechRecognizer.ERROR_NETWORK,
        AndroidSpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "A network error occurred."
        AndroidSpeechRecognizer.ERROR_AUDIO -> "An audio error occurred."
        AndroidSpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy."
        AndroidSpeechRecognizer.ERROR_CLIENT -> "Speech recognition was cancelled."
        else -> "Speech recognition failed."
    }
}
