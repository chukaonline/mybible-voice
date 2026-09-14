package com.mybiblevoice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mybiblevoice.domain.bible.BibleReference
import com.mybiblevoice.domain.bible.BibleReferenceParser
import com.mybiblevoice.domain.parser.ParserResult
import com.mybiblevoice.mysword.LaunchResult
import com.mybiblevoice.mysword.MySwordLauncher
import com.mybiblevoice.speech.SpeechRecognizer
import com.mybiblevoice.speech.SpeechState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Coordinates the core workflow without embedding parsing or Android Intent details
 * (SRS section 13): startListening -> SpeechRecognizer -> recognized text ->
 * BibleReferenceParser -> MySwordLauncher -> MainUiState.
 */
class MainViewModel(
    private val speechRecognizer: SpeechRecognizer,
    private val mySwordLauncher: MySwordLauncher
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            speechRecognizer.state.collect { speechState -> handleSpeechState(speechState) }
        }
    }

    fun onMicTapped() {
        _uiState.update { it.copy(errorMessage = null, ambiguousMessage = null) }
        speechRecognizer.startListening()
    }

    private fun handleSpeechState(speechState: SpeechState) {
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

    private fun handleRecognizedText(text: String) {
        _uiState.update {
            it.copy(status = ListeningStatus.IDLE, recognizedText = text, errorMessage = null)
        }
        when (val result = BibleReferenceParser.parse(text)) {
            is ParserResult.Success -> {
                _uiState.update { it.copy(lastSuccessfulReference = result.reference) }
                launchMySword(result.reference)
            }
            is ParserResult.Ambiguous -> _uiState.update { it.copy(ambiguousMessage = result.message) }
            is ParserResult.Invalid -> _uiState.update { it.copy(errorMessage = result.message) }
            is ParserResult.NotBibleReference -> _uiState.update { it.copy(errorMessage = result.message) }
        }
    }

    private fun launchMySword(reference: BibleReference) {
        when (val launchResult = mySwordLauncher.open(reference)) {
            is LaunchResult.Launched -> _uiState.update {
                it.copy(
                    launchNote = if (launchResult.exactPassage) {
                        null
                    } else {
                        "Opened MySword, but could not navigate to the exact passage on this MySword version."
                    }
                )
            }
            LaunchResult.NotInstalled -> _uiState.update {
                it.copy(errorMessage = "MySword is required but is not installed.")
            }
            is LaunchResult.Failed -> _uiState.update { it.copy(errorMessage = launchResult.reason) }
        }
    }

    override fun onCleared() {
        speechRecognizer.stopListening()
        super.onCleared()
    }
}

class MainViewModelFactory(
    private val speechRecognizer: SpeechRecognizer,
    private val mySwordLauncher: MySwordLauncher
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(speechRecognizer, mySwordLauncher) as T
    }
}
