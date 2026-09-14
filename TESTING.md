# Real-Device Testing Guide

Everything so far (parser, speech wiring, MySword deep-linking, settings) has been verified
by unit tests and on an Android emulator. Two things an emulator cannot give us:

- **Real speech-to-text.** The emulator has no microphone and its offline recognizer has no
  language pack in a headless setup, so "Listening..." never resolves to real text.
- **MySword as the teacher will actually use it** — installed from Google Play, with
  whatever Bible modules they already have, on their own hardware.

This is the checklist for closing that gap on a real Android phone.

## 1. Prerequisites on the phone

1. **Enable Developer Options**: Settings -> About phone -> tap "Build number" 7 times.
2. **Enable USB debugging**: Settings -> System -> Developer options -> USB debugging (on).
3. **Install MySword** from the Google Play Store (simplest — self-updating, matches what
   real users will have). Complete its first-run setup and download at least one Bible
   module, the same way we did on the emulator — MySword refuses external links until
   it's configured (see the walkthrough in the Phase 3 session if you need the exact
   steps: grant "All files access", pick a modules path, download a Bible).
4. Connect the phone to this Mac with a USB cable, and tap **Allow** on the "Allow USB
   debugging?" prompt that appears on the phone (check "Always allow from this computer"
   to skip it next time).

**No cable available?** Pair over Wi-Fi instead: on the phone, Developer options -> Wireless
debugging -> Pair device with pairing code, then:
```bash
export ANDROID_SDK_ROOT="/opt/homebrew/share/android-commandlinetools"
"$ANDROID_SDK_ROOT/platform-tools/adb" pair <ip>:<pairing-port>
"$ANDROID_SDK_ROOT/platform-tools/adb" connect <ip>:<port>
```
(both ports and the pairing code are shown on the phone's Wireless debugging screen).

## 2. Confirm the device is visible

```bash
export ANDROID_SDK_ROOT="/opt/homebrew/share/android-commandlinetools"
"$ANDROID_SDK_ROOT/platform-tools/adb" devices
```
Should list the device as `device` (not `unauthorized` — if so, check the phone screen for
the debugging prompt; not `offline` — try reconnecting the cable).

## 3. Build and install

From the project root:
```bash
export JAVA_HOME="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
./gradlew :app:installDebug
```
This builds the debug APK and installs it straight onto whichever device/emulator `adb`
currently sees. Launch it from the phone's app drawer, or:
```bash
"$ANDROID_SDK_ROOT/platform-tools/adb" shell am start -n com.mybiblevoice/.MainActivity
```

## 4. Watch logs while testing (optional but recommended)

In a separate terminal, leave this running so you can see internal errors that never
surface in the UI:
```bash
"$ANDROID_SDK_ROOT/platform-tools/adb" logcat | grep -i mybiblevoice
```

## 5. Core positive tests — SRS section 18.2's required cases, spoken aloud

Tap **Tap to Speak**, grant the microphone permission when prompted ("While using the app"),
then say each phrase and confirm MySword opens at the expected passage.

| Say this | Expect |
|---|---|
| "Genesis one one" | Genesis 1:1 |
| "John three sixteen" | John 3:16 |
| "John chapter three verse sixteen" | John 3:16 |
| "Romans eight twenty eight" | Romans 8:28 |
| "First Corinthians thirteen four to seven" | 1 Corinthians 13:4-7 |
| "Second Timothy chapter three verse sixteen" | 2 Timothy 3:16 |
| "Psalm twenty three" | Psalms 23 (chapter only) |
| "John three sixteen N L T" | John 3:16, NLT (if NLT is installed in MySword) |
| "Open Genesis one one" | Genesis 1:1 (confirms "open" is stripped) |

For each: watch the app show "Heard: ..." with roughly the right transcription, then confirm
MySword actually comes to the foreground at the right verse. Real speech recognition is not
perfect — if a phrase gets mis-heard, that's a signal about the *recognizer*, not necessarily
our parser; check the "Heard" text against what you actually said before concluding the
parser is wrong.

## 6. Negative / error-path tests — SRS section 17's error table

| Do this | Expect |
|---|---|
| Say something clearly not a Bible reference ("what's the weather today") | "...was not recognized as a Bible reference," no MySword launch |
| Say an ambiguous phrase ("Genesis one two three") | Ambiguous-reference message, no launch |
| Tap the mic, then **Don't allow** the permission dialog | Message explaining the mic permission is required |
| Force-stop or uninstall MySword, then say a valid reference | "MySword is required but is not installed." |
| Say a nonexistent chapter ("Genesis chapter one hundred") | Invalid-chapter message |

## 7. Settings

Open Settings (gear icon, top right):
- Change the speech language, preferred translation, and theme.
- **Kill the app fully** (swipe away from Recents, not just background it) and relaunch.
- Confirm all three choices persisted (DataStore survives process death — that's the point
  of testing this specifically rather than trusting the in-memory `StateFlow`).
- Say a reference *without* naming a translation and confirm your preferred-translation
  setting gets applied; say one *with* an explicit translation (e.g. "...NLT") and confirm
  your setting is correctly ignored in favor of what you said.

## 8. Definition of Done crosswalk (SRS section 23)

| Item | Status |
|---|---|
| Project builds successfully | Done (CI-less local build, verified every phase) |
| 66-book registry implemented | Done, unit-tested |
| Aliases implemented and tested | Done, unit-tested (not exhaustive by design — extend `BibleBookRegistry.extraAliases` as real usage surfaces gaps) |
| Spoken-number parser implemented and tested | Done, unit-tested |
| Reference parser and validator implemented and tested | Done, unit-tested |
| Parser tests pass without a device | Done (`:domain` is pure JVM Kotlin) |
| Speech recognition supplies text to parser | Wired and running; **needs this checklist's section 5 for final confirmation** |
| Valid references generate a verified MySword launch request | Done — confirmed against the real MySword app on the emulator (Phase 3) |
| MySword opens the requested passage on a real Android device | **This checklist's section 5 is the remaining step** |
| Requested translations are never silently replaced | Done by design (code never substitutes; verified by reading `MainViewModel`/`MySwordLauncherImpl`) |
| Missing MySword and invalid/ambiguous references are handled clearly | Done, unit-tested + emulator-verified; **section 6 confirms real-device behavior** |

Everything is ready for this pass — the two rows above marked "needs this checklist" are the
only ones an emulator structurally cannot close out, since they need a real microphone and
Google's real on-device speech service.

## If something fails

- **Nothing happens after "Listening…"**: check the phone actually has a working speech
  service (Settings -> Apps -> Default apps -> Digital assistant app, or similar, varies by
  OEM) and that it has internet access if it needs network-based recognition.
- **Wrong text is heard**: that's the phone's speech engine, not this app — try rephrasing
  or check the accent/language setting under our Settings screen matches how you're speaking.
- **Correct text heard, wrong/no passage opens**: that's on us — capture the exact "Heard"
  text shown in the app plus the logcat output from step 4 and treat it as a parser or
  `MySwordUriBuilder` bug to fix, not a recognizer issue.
