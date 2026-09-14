package com.mybiblevoice.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionDenied = false
            viewModel.onMicTapped()
        } else {
            permissionDenied = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("MyBible Voice", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        Button(onClick = {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                viewModel.onMicTapped()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }) {
            Text("🎤 Tap to Speak")
        }

        Spacer(Modifier.height(24.dp))

        Text(
            when (uiState.status) {
                ListeningStatus.LISTENING -> "Listening…"
                ListeningStatus.PROCESSING -> "Processing…"
                ListeningStatus.IDLE -> "Speak a Bible reference, e.g. \"John 3:16\""
            }
        )

        if (uiState.recognizedText.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Text("Heard: \"${uiState.recognizedText}\"")
        }

        uiState.lastSuccessfulReference?.let { reference ->
            Spacer(Modifier.height(8.dp))
            val verses = when {
                reference.startVerse == null -> ""
                reference.endVerse == null -> ":${reference.startVerse}"
                else -> ":${reference.startVerse}-${reference.endVerse}"
            }
            val translation = reference.translation?.let { " (${it.name})" } ?: ""
            Text("Opening ${reference.book.canonicalName} ${reference.chapter}$verses$translation")
        }

        uiState.launchNote?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.tertiary)
        }

        uiState.ambiguousMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.tertiary)
        }

        uiState.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (permissionDenied) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Microphone permission is required to use voice navigation.",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
