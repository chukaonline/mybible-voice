package com.mybiblevoice.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mybiblevoice.target.BibleTargetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/** No SQLite/Room database (SRS section 15) - DataStore only, for these lightweight preferences. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val SPEECH_LANGUAGE = stringPreferencesKey("speech_language")
        val PREFERRED_TRANSLATION = stringPreferencesKey("preferred_translation")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SELECTED_TARGET = stringPreferencesKey("selected_target")
        val HOLYRICS_HOST = stringPreferencesKey("holyrics_host")
        val HOLYRICS_PORT = intPreferencesKey("holyrics_port")
        val HOLYRICS_TOKEN = stringPreferencesKey("holyrics_token")
        val HOLYRICS_VERSION_MAPPINGS = stringPreferencesKey("holyrics_version_mappings")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            speechLanguageTag = prefs[Keys.SPEECH_LANGUAGE] ?: AppSettings.DEFAULT_SPEECH_LANGUAGE,
            preferredTranslationId = prefs[Keys.PREFERRED_TRANSLATION],
            themeMode = prefs[Keys.THEME_MODE]?.let { raw ->
                runCatching { ThemeMode.valueOf(raw) }.getOrNull()
            } ?: ThemeMode.SYSTEM,
            selectedTarget = prefs[Keys.SELECTED_TARGET]?.let { raw ->
                runCatching { BibleTargetType.valueOf(raw) }.getOrNull()
            } ?: BibleTargetType.MYSWORD,
            holyricsHost = prefs[Keys.HOLYRICS_HOST] ?: "",
            holyricsPort = prefs[Keys.HOLYRICS_PORT] ?: AppSettings.DEFAULT_HOLYRICS_PORT,
            holyricsToken = prefs[Keys.HOLYRICS_TOKEN] ?: "",
            holyricsVersionMappings = decodeVersionMappings(prefs[Keys.HOLYRICS_VERSION_MAPPINGS])
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

    suspend fun setSelectedTarget(target: BibleTargetType) {
        context.settingsDataStore.edit { it[Keys.SELECTED_TARGET] = target.name }
    }

    suspend fun setHolyricsHost(host: String) {
        context.settingsDataStore.edit { it[Keys.HOLYRICS_HOST] = host }
    }

    suspend fun setHolyricsPort(port: Int) {
        context.settingsDataStore.edit { it[Keys.HOLYRICS_PORT] = port }
    }

    suspend fun setHolyricsToken(token: String) {
        context.settingsDataStore.edit { it[Keys.HOLYRICS_TOKEN] = token }
    }

    /** Pass null as [holyricsVersionId] to clear a translation's mapping. */
    suspend fun setHolyricsVersionMapping(translationId: String, holyricsVersionId: String?) {
        context.settingsDataStore.edit { prefs ->
            val current = decodeVersionMappings(prefs[Keys.HOLYRICS_VERSION_MAPPINGS]).toMutableMap()
            if (holyricsVersionId == null) current.remove(translationId) else current[translationId] = holyricsVersionId
            prefs[Keys.HOLYRICS_VERSION_MAPPINGS] = encodeVersionMappings(current)
        }
    }

    private fun decodeVersionMappings(raw: String?): Map<String, String> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val json = JSONObject(raw)
            json.keys().asSequence().associateWith { key -> json.getString(key) }
        }.getOrDefault(emptyMap())
    }

    private fun encodeVersionMappings(mappings: Map<String, String>): String {
        val json = JSONObject()
        mappings.forEach { (translationId, versionId) -> json.put(translationId, versionId) }
        return json.toString()
    }
}
