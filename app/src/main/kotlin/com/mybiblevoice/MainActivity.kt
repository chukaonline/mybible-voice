package com.mybiblevoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mybiblevoice.mysword.MySwordLauncherImpl
import com.mybiblevoice.speech.SpeechRecognizerImpl
import com.mybiblevoice.ui.MainScreen
import com.mybiblevoice.ui.MainViewModel
import com.mybiblevoice.ui.MainViewModelFactory
import com.mybiblevoice.ui.theme.MyBibleVoiceTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            speechRecognizer = SpeechRecognizerImpl(applicationContext),
            mySwordLauncher = MySwordLauncherImpl(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyBibleVoiceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
