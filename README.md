# Magic Kingdom Trivia

Magic Kingdom Trivia is a native, offline Android TV game built for the Nvidia Shield remote. It includes 104 locally stored questions, configurable rounds, timers, immediate answer feedback, persistent settings and statistics, and a television-first Jetpack Compose interface.

Read [PROJECT.md](PROJECT.md) before making changes. It is the long-term source of truth for architecture, behavior, standards, and known limitations.

## Features

- Nine category choices: eight data-driven categories plus Mixed.
- Easy, Medium, Hard, and Mixed difficulty.
- 10, 20, 30, or 50-question rounds with a safe shortage notice.
- Optional 10, 15, or 30-second timer.
- Optional question/answer randomization, explanations, automatic advancement, recent-question avoidance, and generated sound effects.
- Persistent totals, accuracy, category/difficulty performance, high scores, and recent question history.
- D-pad focus, Center Select actions, predictable Back behavior, and active-game restoration.
- No account, backend, ads, analytics, authentication, or internet permission.

## Project Structure

- `app/src/main/assets/questions/core_questions.json` contains the bundled question pack.
- `app/src/main/java/.../data/` loads, validates, filters, and persists local data.
- `app/src/main/java/.../domain/` contains immutable settings, game state, scoring, statistics, and state serialization.
- `app/src/main/java/.../ui/` contains the ViewModel, navigation state, TV screens, focus behavior, and theme.
- `app/src/test/` contains local JVM tests.
- `app/src/androidTest/` contains Compose remote-focus tests that require an emulator or device.

## Requirements

- JDK 17 or newer.
- Android SDK platform 35 and build tools 35.0.0.
- Android Studio is recommended but not required.

Set `ANDROID_HOME`/`ANDROID_SDK_ROOT`, or create an untracked `local.properties` file:

```properties
sdk.dir=/path/to/Android/sdk
```

## Test and Build

Run all local unit tests:

```bash
./gradlew test
```

Build the debug APK:

```bash
./gradlew assembleDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Compile the instrumented Compose tests:

```bash
./gradlew compileDebugAndroidTestKotlin
```

With a compatible emulator or Android TV device connected, run them with:

```bash
./gradlew connectedDebugAndroidTest
```

## Release Build and Signing

Create a release keystore locally, then copy `signing.properties.example` to the ignored file `signing.properties` and set its four values:

```properties
storeFile=/path/to/release-key.jks
storePassword=LOCAL_PASSWORD
keyAlias=release
keyPassword=LOCAL_PASSWORD
```

Build the release APK:

```bash
./gradlew assembleRelease
```

When `signing.properties` exists, Gradle signs the release with that local configuration. Without it, Gradle produces an unsigned release APK for build verification. Never commit the properties file, passwords, or keystore.

Release output is under:

```text
app/build/outputs/apk/release/
```

## Install on Nvidia Shield

1. On the Shield, open Settings and enable Developer options by selecting the build number repeatedly in the About screen.
2. Enable USB or network debugging in Developer options. Menu names vary by Shield Experience version.
3. Find the Shield's IP address in its network settings.
4. From the development computer, connect and install:

```bash
adb connect SHIELD_IP_ADDRESS:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Approve the debugging prompt on the television. Launch **Magic Kingdom Trivia** from the Android TV apps screen.

## Update Questions Only

Edit `app/src/main/assets/questions/core_questions.json`. Every entry must have:

```json
{
  "id": "animation_001",
  "question": "Question text",
  "answers": ["Answer A", "Answer B", "Answer C", "Answer D"],
  "correctAnswerIndex": 0,
  "category": "Disney Animation",
  "difficulty": "Easy",
  "explanation": "A concise explanation.",
  "sourceTitle": "Source title"
}
```

IDs must be stable and unique. Answers must be four nonblank, distinct strings. `correctAnswerIndex` must be 0 through 3, and difficulty must be `Easy`, `Medium`, or `Hard`. Categories are derived from valid JSON entries, so a new category requires no Kotlin change. Invalid entries are skipped and logged instead of crashing the app.

After editing questions, run:

```bash
./gradlew test assembleDebug
```

## Change the Name or Artwork

- Change the app name once in `app/src/main/res/values/strings.xml` (`app_name`). The activity, launcher, and Compose home screen all use that resource.
- Replace `app/src/main/res/drawable/tv_banner.xml` for the 320 x 180 TV banner.
- Replace `app/src/main/res/drawable/ic_launcher_foreground.xml` and related adaptive icon resources for launcher artwork.
- Use only original or properly licensed assets; do not add protected character art, logos, stills, or music.

## Git Workflow

Never commit directly to `main`. Work on a `codex/` feature branch, run tests and a debug build, use descriptive commits, and open a pull request into `main`.
