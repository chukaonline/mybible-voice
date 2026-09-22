package com.mybiblevoice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mybiblevoice.data.settings.AppSettings
import com.mybiblevoice.data.settings.SettingsRepository
import com.mybiblevoice.data.settings.ThemeMode
import com.mybiblevoice.domain.bible.BibleReference
import com.mybiblevoice.domain.bible.BibleReferenceParser
import com.mybiblevoice.domain.parser.ParserResult
import com.mybiblevoice.domain.translation.TranslationRegistry
import com.mybiblevoice.holyrics.HolyricsApi
import com.mybiblevoice.holyrics.HolyricsConnectionConfig
import com.mybiblevoice.holyrics.HolyricsResult
import com.mybiblevoice.holyrics.HolyricsVersionRepository
import com.mybiblevoice.speech.SpeechRecognizer
import com.mybiblevoice.speech.SpeechState
import com.mybiblevoice.target.BibleTarget
import com.mybiblevoice.target.BibleTargetType
import com.mybiblevoice.target.TargetResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordinates the core workflow without embedding parsing, Android Intent, or Holyrics HTTP
 * details (SRS section 13; extended by the Holyrics integration design section 19):
 * startListening -> SpeechRecognizer -> recognized text -> BibleReferenceParser ->
 * selected BibleTarget -> MainUiState.
 */
class MainViewModel(
    private val speechRecognizer: SpeechRecognizer,
    private val targets: Map<BibleTargetType, BibleTarget>,
    private val settingsRepository: SettingsRepository,
    private val holyricsApi: HolyricsApi,
    private val holyricsVersionRepository: HolyricsVersionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _holyricsSettingsUiState = MutableStateFlow(HolyricsSettingsUiState())
    val holyricsSettingsUiState: StateFlow<HolyricsSettingsUiState> = _holyricsSettingsUiState.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    init {
        viewModelScope.launch {
            speechRecognizer.state.collect { speechState -> handleSpeechState(speechState) }
        }
    }

    fun onMicTapped() {
        _uiState.update { it.copy(errorMessage = null, ambiguousMessage = null, launchNote = null) }
        speechRecognizer.startListening(settings.value.speechLanguageTag)
    }

    fun onSpeechLanguageChanged(languageTag: String) {
        viewModelScope.launch { settingsRepository.setSpeechLanguage(languageTag) }
    }

    fun onPreferredTranslationChanged(translationId: String?) {
        viewModelScope.launch { settingsRepository.setPreferredTranslation(translationId) }
    }

    fun onThemeModeChanged(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun onTargetChanged(target: BibleTargetType) {
        viewModelScope.launch { settingsRepository.setSelectedTarget(target) }
    }

    fun onHolyricsHostChanged(host: String) {
        viewModelScope.launch { settingsRepository.setHolyricsHost(host) }
    }

    fun onHolyricsPortChanged(port: Int) {
        viewModelScope.launch { settingsRepository.setHolyricsPort(port) }
    }

    fun onHolyricsTokenChanged(token: String) {
        viewModelScope.launch { settingsRepository.setHolyricsToken(token) }
    }

    fun onHolyricsVersionMappingChanged(translationId: String, holyricsVersionId: String?) {
        viewModelScope.launch { settingsRepository.setHolyricsVersionMapping(translationId, holyricsVersionId) }
    }

    /** Side-effect-free per design section 12 - never presents a verse, just proves the
     *  configured host/port/token actually work. */
    fun testHolyricsConnection() {
        viewModelScope.launch {
            _holyricsSettingsUiState.update { it.copy(connectionTestStatus = ConnectionTestStatus.Testing) }
            val result = holyricsApi.getTokenInfo(currentHolyricsConfig())
            _holyricsSettingsUiState.update {
                it.copy(
                    connectionTestStatus = when (result) {
                        is HolyricsResult.Success ->
                            ConnectionTestStatus.Success("Connected - Holyrics v${result.value.holyricsVersion}")
                        is HolyricsResult.Failure -> ConnectionTestStatus.Failure(result.error.message)
                    }
                )
            }
        }
    }

    fun loadHolyricsVersions() {
        viewModelScope.launch {
            _holyricsSettingsUiState.update { it.copy(isLoadingVersions = true, versionsError = null) }
            val result = holyricsVersionRepository.fetchAvailableVersions(currentHolyricsConfig())
            _holyricsSettingsUiState.update {
                it.copy(
                    isLoadingVersions = false,
                    availableVersions = result.getOrDefault(emptyList()),
                    versionsError = result.exceptionOrNull()?.message
                )
            }
        }
    }

    private fun currentHolyricsConfig(): HolyricsConnectionConfig {
        val current = settings.value
        return HolyricsConnectionConfig(
            host = current.holyricsHost,
            port = current.holyricsPort,
            token = current.holyricsToken
        )
    }

    private suspend fun handleSpeechState(speechState: SpeechState) {
        when (speechState) {
            SpeechState.Idle -> _uiState.update { it.copy(status = ListeningStatus.IDLE) }
            SpeechState.Listening -> _uiState.update {
                it.copy(status = ListeningStatus.LISTENING, errorMessage = null)
            }
            SpeechState.Processing -> _uiState.update { it.copy(status = ListeningStatus.PROCESSING) }
            is SpeechState.Success -> handleRecognizedText(speechState.recognizedText)
            is SpeechState.Error -> _uiState.update {
                it.copy(status = ListeningStatus.IDLE, errorMessage = speechState.message)
            }
        }
    }

    private suspend fun handleRecognizedText(text: String) {
        _uiState.update {
            it.copy(status = ListeningStatus.IDLE, recognizedText = text, errorMessage = null)
        }
        when (val result = BibleReferenceParser.parse(text)) {
            is ParserResult.Success -> {
                val reference = applyPreferredTranslationIfUnspoken(result.reference)
                _uiState.update { it.copy(lastSuccessfulReference = reference) }
                dispatchToSelectedTarget(reference)
            }
            is ParserResult.Ambiguous -> _uiState.update { it.copy(ambiguousMessage = result.message) }
            is ParserResult.Invalid -> _uiState.update { it.copy(errorMessage = result.message) }
            is ParserResult.NotBibleReference -> _uiState.update { it.copy(errorMessage = result.message) }
        }
    }

    /**
     * A translation named IN SPEECH is never touched. Only when the user didn't say one do
     * we fill in their configured preferred translation (SRS section 15) - this fills a gap
     * the user left, it does not substitute a translation they actually requested.
     */
    private fun applyPreferredTranslationIfUnspoken(reference: BibleReference): BibleReference {
        if (reference.translation != null) return reference
        val preferredId = settings.value.preferredTranslationId ?: return reference
        val preferred = TranslationRegistry.findById(preferredId) ?: return reference
        return reference.copy(translation = preferred)
    }

    private suspend fun dispatchToSelectedTarget(reference: BibleReference) {
        val target = targets.getValue(settings.value.selectedTarget)
        when (val result = target.open(reference)) {
            is TargetResult.Success -> _uiState.update { it.copy(launchNote = result.warning) }
            is TargetResult.Failure -> _uiState.update { it.copy(errorMessage = result.error.message) }
        }
    }

    override fun onCleared() {
        speechRecognizer.stopListening()
        super.onCleared()
    }
}

class MainViewModelFactory(
    private val speechRecognizer: SpeechRecognizer,
    private val targets: Map<BibleTargetType, BibleTarget>,
    private val settingsRepository: SettingsRepository,
    private val holyricsApi: HolyricsApi,
    private val holyricsVersionRepository: HolyricsVersionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(speechRecognizer, targets, settingsRepository, holyricsApi, holyricsVersionRepository) as T
    }
}
