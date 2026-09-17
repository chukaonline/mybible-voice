# MyBible Voice

A lightweight Android utility for Bible teachers: speak a Bible reference, and
[MySword](https://www.mysword.info/) opens directly at that passage. It is a voice-controlled
navigation layer around MySword, not a replacement Bible app — MySword remains responsible for
all Bible text, reading, and study functionality.

```
"John three sixteen"  -->  MySword opens at John 3:16
```

## Status

**v0.1 complete**, verified end-to-end on a real Android device — positive references, negative/
error cases, and settings persistence all passing. See [TESTING.md](TESTING.md) for the full test
script and a line-by-line Definition-of-Done crosswalk.

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

## Requirements

- JDK 17
- Android SDK (compileSdk 34, minSdk 24) — the Gradle wrapper handles the rest
- [MySword](https://www.mysword.info/) installed on the target device, with at least one Bible
  module downloaded

## Build & test

```bash
./gradlew :domain:test              # parser/domain unit tests (no device needed)
./gradlew :app:testDebugUnitTest    # app-module unit tests (URI building, etc.)
./gradlew :app:assembleDebug        # build the debug APK
./gradlew :app:installDebug         # build and install to a connected device/emulator
```

60 unit tests currently pass across both modules.

## Real-device testing

[TESTING.md](TESTING.md) has the full walkthrough: device setup, the exact phrases to speak for
every required positive/negative case, a settings-persistence check, and what to do if something
doesn't work as expected.

## Explicitly out of scope for v0.1

No built-in Bible text or reader, no module downloads, no sermon/presentation tooling, no
accounts, no backend, no AI interpretation, no always-on listening, no audio recording. This is a
navigation layer, deliberately kept small.
