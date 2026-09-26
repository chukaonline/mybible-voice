package com.mybiblevoice.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mybiblevoice.data.settings.AppSettings
import com.mybiblevoice.data.settings.ThemeMode
import com.mybiblevoice.domain.translation.TranslationRegistry
import com.mybiblevoice.holyrics.HolyricsBibleVersion
import com.mybiblevoice.holyrics.parseHolyricsQrPayload
import com.mybiblevoice.target.BibleTargetType
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

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
    holyricsSettingsState: HolyricsSettingsUiState,
    onSpeechLanguageChange: (String) -> Unit,
    onPreferredTranslationChange: (String?) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onTargetChange: (BibleTargetType) -> Unit,
    onHolyricsHostChange: (String) -> Unit,
    onHolyricsPortChange: (Int) -> Unit,
    onHolyricsTokenChange: (String) -> Unit,
    onHolyricsVersionMappingChange: (String, String?) -> Unit,
    onTestHolyricsConnection: () -> Unit,
    onLoadHolyricsVersions: () -> Unit,
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
            item { SectionHeader("Bible target") }
            items(BibleTargetType.entries.toList()) { target ->
                SelectableRow(
                    label = if (target == BibleTargetType.MYSWORD) "MySword" else "Holyrics",
                    selected = settings.selectedTarget == target,
                    onClick = { onTargetChange(target) }
                )
            }

            if (settings.selectedTarget == BibleTargetType.HOLYRICS) {
                item { HorizontalDivider() }
                item {
                    HolyricsConnectionSection(
                        settings = settings,
                        connectionState = holyricsSettingsState,
                        onHostChange = onHolyricsHostChange,
                        onPortChange = onHolyricsPortChange,
                        onTokenChange = onHolyricsTokenChange,
                        onTestConnection = onTestHolyricsConnection
                    )
                }

                item { HorizontalDivider() }
                item {
                    HolyricsVersionMappingSection(
                        settings = settings,
                        versionsState = holyricsSettingsState,
                        onLoadVersions = onLoadHolyricsVersions,
                        onMappingChange = onHolyricsVersionMappingChange
                    )
                }
            }

            item { HorizontalDivider() }
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
                    label = "Target default",
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
private fun HolyricsConnectionSection(
    settings: AppSettings,
    connectionState: HolyricsSettingsUiState,
    onHostChange: (String) -> Unit,
    onPortChange: (Int) -> Unit,
    onTokenChange: (String) -> Unit,
    onTestConnection: () -> Unit
) {
    // Local, editable copies - seeded once from `settings` and never overwritten by it again
    // while this section stays composed. Binding the fields directly to `settings.holyricsHost`
    // (etc.) made every keystroke wait on an async DataStore write-then-re-read before the
    // field would show it; when that echo landed out of order mid-typing it could scramble/
    // reverse what the user had just entered. Typing now only updates this local state -
    // persistence still happens via onHostChange/onPortChange/onTokenChange, it just no longer
    // drives what's rendered.
    var hostText by remember { mutableStateOf(settings.holyricsHost) }
    var portText by remember { mutableStateOf(settings.holyricsPort.toString()) }
    var tokenText by remember { mutableStateOf(settings.holyricsToken) }
    var qrError by remember { mutableStateOf<String?>(null) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val raw = result.contents ?: return@rememberLauncherForActivityResult
        val parsed = parseHolyricsQrPayload(raw)
        if (parsed == null) {
            qrError = "Could not read Holyrics connection details from that QR code."
        } else {
            qrError = null
            hostText = parsed.host
            portText = parsed.port.toString()
            tokenText = parsed.token
            onHostChange(parsed.host)
            onPortChange(parsed.port)
            onTokenChange(parsed.token)
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader("Holyrics connection")

        Button(
            onClick = {
                qrError = null
                scanLauncher.launch(
                    ScanOptions()
                        .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        .setPrompt("Scan the QR code from Holyrics' API Server settings")
                        .setBeepEnabled(false)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Scan Holyrics QR code") }
        qrError?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = hostText,
            onValueChange = {
                hostText = it
                onHostChange(it)
            },
            label = { Text("Host / IP") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = portText,
            onValueChange = { text ->
                portText = text
                text.toIntOrNull()?.let(onPortChange)
            },
            label = { Text("Port") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = tokenText,
            onValueChange = {
                tokenText = it
                onTokenChange(it)
            },
            label = { Text("API token") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onTestConnection) { Text("Test Connection") }
            Spacer(Modifier.width(12.dp))
            when (val status = connectionState.connectionTestStatus) {
                ConnectionTestStatus.Idle -> Unit
                ConnectionTestStatus.Testing -> CircularProgressIndicator(modifier = Modifier.height(20.dp))
                is ConnectionTestStatus.Success -> Text(status.message, color = MaterialTheme.colorScheme.primary)
                is ConnectionTestStatus.Failure -> Text(status.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


@Composable
private fun HolyricsVersionMappingSection(
    settings: AppSettings,
    versionsState: HolyricsSettingsUiState,
    onLoadVersions: () -> Unit,
    onMappingChange: (String, String?) -> Unit
) {
    var expandedTranslationId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader("Bible version mappings")
        Button(onClick = onLoadVersions) { Text("Discover Holyrics Bible versions") }
        Spacer(Modifier.height(8.dp))

        if (versionsState.isLoadingVersions) {
            CircularProgressIndicator(modifier = Modifier.height(20.dp))
        }
        versionsState.versionsError?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (versionsState.availableVersions.isNotEmpty()) {
            TranslationRegistry.translations.forEach { translation ->
                val mappedVersionId = settings.holyricsVersionMappings[translation.id]
                val mappedTitle = versionsState.availableVersions
                    .firstOrNull { it.id == mappedVersionId }?.title
                    ?: mappedVersionId

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedTranslationId =
                                if (expandedTranslationId == translation.id) null else translation.id
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(translation.name, modifier = Modifier.fillMaxWidth().padding(end = 8.dp))
                }
                Text(
                    mappedTitle ?: "Not mapped",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (expandedTranslationId == translation.id) {
                    SelectableRow(
                        label = "Not mapped",
                        selected = mappedVersionId == null,
                        onClick = { onMappingChange(translation.id, null) }
                    )
                    versionsState.availableVersions.forEach { version: HolyricsBibleVersion ->
                        SelectableRow(
                            label = version.title,
                            selected = mappedVersionId == version.id,
                            onClick = { onMappingChange(translation.id, version.id) }
                        )
                    }
                }
                HorizontalDivider()
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
