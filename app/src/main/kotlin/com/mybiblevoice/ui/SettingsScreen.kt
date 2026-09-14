package com.mybiblevoice.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mybiblevoice.data.settings.AppSettings
import com.mybiblevoice.data.settings.ThemeMode
import com.mybiblevoice.domain.translation.TranslationRegistry

private data class LanguageOption(val tag: String, val label: String)

private val SPEECH_LANGUAGE_OPTIONS = listOf(
    LanguageOption("en-US", "English (US)"),
    LanguageOption("en-GB", "English (UK)"),
    LanguageOption("en-AU", "English (Australia)"),
    LanguageOption("en-IN", "English (India)"),
    LanguageOption("en-CA", "English (Canada)")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSpeechLanguageChange: (String) -> Unit,
    onPreferredTranslationChange: (String?) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item { SectionHeader("Speech recognition language") }
            items(SPEECH_LANGUAGE_OPTIONS) { option ->
                SelectableRow(
                    label = option.label,
                    selected = settings.speechLanguageTag == option.tag,
                    onClick = { onSpeechLanguageChange(option.tag) }
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("Preferred translation") }
            item {
                SelectableRow(
                    label = "MySword default",
                    selected = settings.preferredTranslationId == null,
                    onClick = { onPreferredTranslationChange(null) }
                )
            }
            items(TranslationRegistry.translations) { translation ->
                SelectableRow(
                    label = translation.name,
                    selected = settings.preferredTranslationId == translation.id,
                    onClick = { onPreferredTranslationChange(translation.id) }
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("Theme") }
            items(ThemeMode.entries.toList()) { mode ->
                SelectableRow(
                    label = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = settings.themeMode == mode,
                    onClick = { onThemeModeChange(mode) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SelectableRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}
