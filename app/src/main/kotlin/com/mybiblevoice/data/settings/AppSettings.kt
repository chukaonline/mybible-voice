package com.mybiblevoice.data.settings

import com.mybiblevoice.target.BibleTargetType

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * MVP settings (SRS section 15, extended by the Holyrics integration design section 15):
 * speech-recognition language, an optional preferred translation used when the spoken
 * reference didn't name one, basic UI preferences, the active Bible target, and Holyrics
 * connection/version-mapping state.
 *
 * New fields all default such that an existing installation upgrading from before this
 * feature existed keeps behaving exactly as it did (MySword selected, Holyrics unconfigured).
 */
data class AppSettings(
    val speechLanguageTag: String = DEFAULT_SPEECH_LANGUAGE,
    val preferredTranslationId: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedTarget: BibleTargetType = BibleTargetType.MYSWORD,
    val holyricsHost: String = "",
    val holyricsPort: Int = DEFAULT_HOLYRICS_PORT,
    val holyricsToken: String = "",
    /** Maps this app's [com.mybiblevoice.domain.translation.Translation.id] to a Holyrics
     *  Bible version ID (the `version` field from GetBibleVersionsV2) - authoritative for the
     *  user's actual Holyrics installation, since two installations need not have the same
     *  Bible modules. */
    val holyricsVersionMappings: Map<String, String> = emptyMap()
) {
    companion object {
        const val DEFAULT_SPEECH_LANGUAGE = "en-US"

        /** Holyrics' commonly documented default API Server port - never assumed unchanged. */
        const val DEFAULT_HOLYRICS_PORT = 8091
    }
}
