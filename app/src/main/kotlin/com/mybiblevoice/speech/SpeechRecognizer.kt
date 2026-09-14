package com.mybiblevoice.speech

import kotlinx.coroutines.flow.StateFlow

/**
 * Abstraction over Android's speech recognition so the rest of the app (and tests)
 * never depend on [android.speech.SpeechRecognizer] directly.
 */
interface SpeechRecognizer {
    val state: StateFlow<SpeechState>

    /** @param languageTag a BCP-47 tag (e.g. "en-US") - the user's speech-recognition language preference. */
    fun startListening(languageTag: String)
    fun stopListening()
}
