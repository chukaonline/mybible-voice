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
import com.mybiblevoice.mysword.MySwordLauncherImpl
import com.mybiblevoice.speech.SpeechRecognizerImpl
import com.mybiblevoice.ui.MainScreen
import com.mybiblevoice.ui.MainViewModel
import com.mybiblevoice.ui.MainViewModelFactory
import com.mybiblevoice.ui.SettingsScreen
import com.mybiblevoice.ui.theme.MyBibleVoiceTheme

private sealed class Screen {
    object Main : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            speechRecognizer = SpeechRecognizerImpl(applicationContext),
            mySwordLauncher = MySwordLauncherImpl(applicationContext),
            settingsRepository = SettingsRepository(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by viewModel.settings.collectAsState()
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
                            onSpeechLanguageChange = viewModel::onSpeechLanguageChanged,
                            onPreferredTranslationChange = viewModel::onPreferredTranslationChanged,
                            onThemeModeChange = viewModel::onThemeModeChanged,
                            onBack = { screen = Screen.Main }
                        )
                    }
                }
            }
        }
    }
}
