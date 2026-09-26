# MyBible Voice

A lightweight Android utility for Bible teachers: speak a Bible reference, and it opens directly
at that passage — in [MySword](https://www.mysword.info/) on the same device, or on a
Holyrics presentation screen over your local network. It is a voice-controlled navigation layer,
not a replacement Bible app — MySword/Holyrics remain responsible for all Bible text, reading,
and presentation.

```
"John three sixteen"  -->  MySword opens at John 3:16
                       -->  or Holyrics presents John 3:16, if that's your selected target
```

## Download

Grab the latest APK from the [Releases page](https://github.com/chukaonline/mybible-voice/releases/latest)
and install it on your Android phone.

- For **MySword**: install it from Google Play, with at least one Bible module downloaded.
- For **Holyrics**: no phone-side install needed beyond this app — it talks to Holyrics' own
  API Server over your local Wi-Fi. In Settings, select "Holyrics" as the target, then either
  tap **Scan Holyrics QR code** and scan the QR code from Holyrics' API Server settings screen,
  or enter the host/IP, port, and API token shown there by hand.

## Status

**v0.2.0**, verified end-to-end on real Android devices — positive references, negative/error
cases, settings persistence, and (for Holyrics) live connection/version-discovery/verse display,
QR-code connection setup, and Next/Previous verse navigation against a real Holyrics
installation all passing.

## How it works

1. Tap the microphone.
2. Speak a reference, optionally including a translation ("John 3:16 NLT").
3. Android's speech recognizer converts it to text.
4. A pure-Kotlin parser turns the text into a structured, validated reference.
5. MyBible Voice dispatches it to whichever target is selected in Settings — MySword, launched
   via its documented external-link mechanism, or Holyrics, presented via its local-network API
   Server.

The parser is deliberately conservative: if a spoken phrase is genuinely ambiguous, it asks again
rather than guessing.

## Architecture

Two Gradle modules, split along a hard boundary rather than just convention:

- **`:domain`** — pure Kotlin, zero Android dependencies, runs on the plain JVM. The 66-book
  registry (with alias/ordinal matching), spoken-number parser, text normalizer, reference parser
  and validator, and translation registry all live here, fully unit-tested without a device or
  emulator.
- **`:app`** — the Android application (Kotlin, Jetpack Compose, lightweight MVVM). Wraps
  `android.speech.SpeechRecognizer`, holds the Compose UI and DataStore-backed settings (speech
  language, preferred translation, theme, selected target, Holyrics connection), and dispatches
  a parsed reference to whichever `BibleTarget` is selected:
  - **MySword** — builds and fires MySword's external-link Intent.
  - **Holyrics** — an OkHttp client against Holyrics' local-network API Server (`ShowVerse`,
    `GetBibleVersionsV2`, `GetTokenInfo`), with per-translation version mapping (a translation
    with no mapped Holyrics version fails explicitly rather than silently substituting one) and
    an optional QR-code scan (ZXing) to fill in the connection details Holyrics' own settings
    screen displays as a QR code.

```
com.mybiblevoice/
├── MainActivity.kt
├── ui/            MainScreen, SettingsScreen, MainViewModel, theme
├── speech/        SpeechRecognizer interface + Android implementation
├── target/        BibleTarget interface, TargetResult, TargetError - shared by both targets
├── mysword/       MySwordTarget, MySwordLauncher, MySwordUriBuilder, book/translation codes
├── holyrics/      HolyricsTarget, HolyricsApiClient, HolyricsReferenceMapper,
│                  HolyricsVersionRepository, HolyricsQrPayload
└── data/settings/ DataStore-backed AppSettings

domain (com.mybiblevoice.domain)/
├── bible/         BibleBook, BibleBookRegistry, BibleReference, parser, validator
├── parser/        SpokenNumberParser, TextNormalizer, ParserResult
└── translation/    Translation, TranslationRegistry
```
