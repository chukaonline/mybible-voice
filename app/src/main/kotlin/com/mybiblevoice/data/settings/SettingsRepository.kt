package com.mybiblevoice.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** No SQLite/Room database (SRS section 15) - DataStore only, for these lightweight preferences. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val SPEECH_LANGUAGE = stringPreferencesKey("speech_language")
        val PREFERRED_TRANSLATION = stringPreferencesKey("preferred_translation")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            speechLanguageTag = prefs[Keys.SPEECH_LANGUAGE] ?: AppSettings.DEFAULT_SPEECH_LANGUAGE,
            preferredTranslationId = prefs[Keys.PREFERRED_TRANSLATION],
            themeMode = prefs[Keys.THEME_MODE]?.let { raw ->
                runCatching { ThemeMode.valueOf(raw) }.getOrNull()
            } ?: ThemeMode.SYSTEM
        )
    }

    suspend fun setSpeechLanguage(languageTag: String) {
        context.settingsDataStore.edit { it[Keys.SPEECH_LANGUAGE] = languageTag }
    }

    /** Pass null to clear the preference and use MySword's current/default Bible instead. */
    suspend fun setPreferredTranslation(translationId: String?) {
        context.settingsDataStore.edit { prefs ->
            if (translationId == null) {
                prefs.remove(Keys.PREFERRED_TRANSLATION)
            } else {
                prefs[Keys.PREFERRED_TRANSLATION] = translationId
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }
}
