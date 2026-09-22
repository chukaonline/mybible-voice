package com.mybiblevoice.ui

import com.mybiblevoice.holyrics.HolyricsBibleVersion

sealed class ConnectionTestStatus {
    object Idle : ConnectionTestStatus()
    object Testing : ConnectionTestStatus()
    data class Success(val message: String) : ConnectionTestStatus()
    data class Failure(val message: String) : ConnectionTestStatus()
}

data class HolyricsSettingsUiState(
    val connectionTestStatus: ConnectionTestStatus = ConnectionTestStatus.Idle,
    val isLoadingVersions: Boolean = false,
    val availableVersions: List<HolyricsBibleVersion> = emptyList(),
    val versionsError: String? = null
)
