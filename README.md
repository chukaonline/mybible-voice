# MyBible Voice

A lightweight Android utility for Bible teachers: speak a Bible reference, and
[MySword](https://www.mysword.info/) opens directly at that passage. It is a voice-controlled
navigation layer around MySword, not a replacement Bible app — MySword remains responsible for
all Bible text, reading, and study functionality.

```
"John three sixteen"  -->  MySword opens at John 3:16
```

## Download

Grab the latest APK from the [Releases page](https://github.com/chukaonline/mybible-voice/releases/latest)
and install it on your Android phone. [MySword](https://www.mysword.info/) must already be
installed (from Google Play) with at least one Bible module downloaded.

## Status

**v0.1 complete**, verified end-to-end on a real Android device — positive references, negative/
error cases, and settings persistence all passing.

## How it works

1. Tap the microphone.
2. Speak a reference, optionally including a translation ("John 3:16 NLT").
3. Android's speech recognizer converts it to text.
4. A pure-Kotlin parser turns the text into a structured, validated reference.
5. MyBible Voice launches MySword at that exact passage via its documented external-link
   mechanism.

The parser is deliberately conservative: if a spoken phrase is genuinely ambiguous, it asks again
rather than guessing.

## Architecture

Two Gradle modules, split along a hard boundary rather than just convention:

- **`:domain`** — pure Kotlin, zero Android dependencies, runs on the plain JVM. The 66-book
  registry (with alias/ordinal matching), spoken-number parser, text normalizer, reference parser
  and validator, and translation registry all live here, fully unit-tested without a device or
  emulator.
- **`:app`** — the Android application (Kotlin, Jetpack Compose, lightweight MVVM). Wraps
  `android.speech.SpeechRecognizer`, builds and fires MySword's external-link Intent, and holds
  the Compose UI and DataStore-backed settings (speech language, preferred translation, theme).

```
com.mybiblevoice/
├── MainActivity.kt
├── ui/            MainScreen, SettingsScreen, MainViewModel, theme
├── speech/        SpeechRecognizer interface + Android implementation
├── mysword/       MySwordLauncher, MySwordUriBuilder, book/translation codes
└── data/settings/ DataStore-backed AppSettings

domain (com.mybiblevoice.domain)/
├── bible/         BibleBook, BibleBookRegistry, BibleReference, parser, validator
├── parser/        SpokenNumberParser, TextNormalizer, ParserResult
└── translation/    Translation, TranslationRegistry
```
