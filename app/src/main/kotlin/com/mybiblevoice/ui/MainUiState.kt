package com.mybiblevoice.ui

import com.mybiblevoice.domain.bible.BibleReference

enum class ListeningStatus { IDLE, LISTENING, PROCESSING }

data class MainUiState(
    val status: ListeningStatus = ListeningStatus.IDLE,
    val recognizedText: String = "",
    val lastSuccessfulReference: BibleReference? = null,
    val ambiguousMessage: String? = null,
    val errorMessage: String? = null,
    val launchNote: String? = null
)
