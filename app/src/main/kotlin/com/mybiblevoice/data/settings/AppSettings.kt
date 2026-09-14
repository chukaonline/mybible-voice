package com.mybiblevoice.data.settings

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * MVP settings (SRS section 15): speech-recognition language, an optional preferred
 * translation used when the spoken reference didn't name one, and basic UI preferences.
 */
data class AppSettings(
    val speechLanguageTag: String = DEFAULT_SPEECH_LANGUAGE,
    val preferredTranslationId: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
) {
    companion object {
        const val DEFAULT_SPEECH_LANGUAGE = "en-US"
    }
}
