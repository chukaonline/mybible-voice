package com.mybiblevoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mybiblevoice.data.settings.SettingsRepository
import com.mybiblevoice.data.settings.ThemeMode
import com.mybiblevoice.holyrics.HolyricsApiClient
import com.mybiblevoice.holyrics.HolyricsConnectionConfig
import com.mybiblevoice.holyrics.HolyricsTarget
import com.mybiblevoice.holyrics.HolyricsVersionRepository
import com.mybiblevoice.mysword.MySwordLauncherImpl
import com.mybiblevoice.mysword.MySwordTarget
import com.mybiblevoice.speech.SpeechRecognizerImpl
import com.mybiblevoice.target.BibleTargetType
import com.mybiblevoice.ui.MainScreen
import com.mybiblevoice.ui.MainViewModel
import com.mybiblevoice.ui.MainViewModelFactory
import com.mybiblevoice.ui.SettingsScreen
import com.mybiblevoice.ui.theme.MyBibleVoiceTheme
import kotlinx.coroutines.flow.first

private sealed class Screen {
    object Main : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private val settingsRepository by lazy { SettingsRepository(applicationContext) }
    private val holyricsApiClient by lazy { HolyricsApiClient() }

    private val viewModel: MainViewModel by viewModels {
        val holyricsTarget = HolyricsTarget(
            apiClient = holyricsApiClient,
            connectionConfigProvider = {
                val current = settingsRepository.settings.first()
                HolyricsConnectionConfig(current.holyricsHost, current.holyricsPort, current.holyricsToken)
            },
            versionMappingProvider = { settingsRepository.settings.first().holyricsVersionMappings }
        )

        MainViewModelFactory(
            speechRecognizer = SpeechRecognizerImpl(applicationContext),
            targets = mapOf(
                BibleTargetType.MYSWORD to MySwordTarget(MySwordLauncherImpl(applicationContext)),
                BibleTargetType.HOLYRICS to holyricsTarget
            ),
            settingsRepository = settingsRepository,
            holyricsApi = holyricsApiClient,
            holyricsVersionRepository = HolyricsVersionRepository(holyricsApiClient)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by viewModel.settings.collectAsState()
            val holyricsSettingsState by viewModel.holyricsSettingsUiState.collectAsState()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            MyBibleVoiceTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var screen by remember { mutableStateOf<Screen>(Screen.Main) }
                    when (screen) {
                        Screen.Main -> MainScreen(
                            viewModel = viewModel,
                            onOpenSettings = { screen = Screen.Settings }
                        )
                        Screen.Settings -> SettingsScreen(
                            settings = settings,
                            holyricsSettingsState = holyricsSettingsState,
                            onSpeechLanguageChange = viewModel::onSpeechLanguageChanged,
                            onPreferredTranslationChange = viewModel::onPreferredTranslationChanged,
                            onThemeModeChange = viewModel::onThemeModeChanged,
                            onTargetChange = viewModel::onTargetChanged,
                            onHolyricsHostChange = viewModel::onHolyricsHostChanged,
                            onHolyricsPortChange = viewModel::onHolyricsPortChanged,
                            onHolyricsTokenChange = viewModel::onHolyricsTokenChanged,
                            onHolyricsVersionMappingChange = viewModel::onHolyricsVersionMappingChanged,
                            onTestHolyricsConnection = viewModel::testHolyricsConnection,
                            onLoadHolyricsVersions = viewModel::loadHolyricsVersions,
                            onBack = { screen = Screen.Main }
                        )
                    }
                }
            }
        }
    }
}
