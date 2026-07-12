# Project Overview

Magic Kingdom Trivia is a native Android TV trivia game designed for Nvidia Shield TV and operated entirely with the Shield remote. It is intended to feel like a polished family game show rather than a mobile interface displayed on a television.

The application runs offline with no phone, browser, account, server, subscription, advertisements, analytics, or authentication. The bundled question pack, settings, game restoration state, and statistics all remain on the device. The manifest does not request internet access.

# Core Principles

Development must prioritize:

- Fast performance.
- Clean architecture.
- Family-friendly design and content.
- A beautiful television interface.
- Extremely reliable remote navigation.
- Easy expansion of the question database.
- Maintainability over cleverness.

Avoid unnecessary complexity. Add dependencies or architectural layers only when they solve a demonstrated project need.

# Technology Stack

The project currently uses:

- Kotlin 2.2.21.
- Android Gradle Plugin 8.13.2.
- Gradle 8.13 through the checked-in wrapper.
- Java/Kotlin target 17.
- Jetpack Compose with the Compose BOM `2026.06.01`.
- Compose for TV: `tv-foundation:1.0.0` and `tv-material:1.1.0`.
- TV Material 3 components and a custom dark color scheme.
- AndroidX Activity Compose and Lifecycle ViewModel/SavedState support.
- Preferences DataStore 1.2.1 for settings and statistics.
- Kotlinx Serialization JSON 1.9.0 for question parsing and state/statistics serialization.
- JUnit 4 for local tests.
- AndroidX Test 1.7.0, Ext JUnit 1.3.0, and Compose UI Test for instrumented focus tests.
- Git and GitHub for version control and review.
- Android Studio as the intended IDE.

No backend, database server, dependency injection framework, analytics SDK, advertising SDK, authentication library, or network library is used.

# Architecture

The application uses a small layered architecture:

1. `MainActivity` configures immersive system bars and hosts Compose.
2. `TriviaApp` renders the current screen and sends user actions to `TriviaViewModel`.
3. `TriviaViewModel` owns navigation, exposes immutable game snapshots, coordinates repositories, records completed games, and saves active state.
4. `TriviaGameEngine` performs pure game transitions: filtering, selection, answer presentation, scoring, timers, progression, and result analysis.
5. `QuestionRepository` abstracts question sources. `AssetQuestionRepository` loads the bundled JSON pack through `QuestionJsonParser`.
6. `SettingsRepository` and `StatisticsRepository` persist small local data in one Preferences DataStore.
7. `GameStateCodec` serializes the active `TriviaGameState` into `SavedStateHandle` for activity/process recreation where Android restores saved state.

UI composables never read the JSON asset or DataStore directly. They receive state and callbacks from the ViewModel. The game engine depends on the `QuestionRepository` interface rather than Android assets, which keeps domain tests fast and permits future question-pack sources.

Navigation is intentionally lightweight rather than using Navigation Compose. `TriviaScreen` represents Home, Categories, Difficulty, Settings, Statistics, Question, and Results. Top-level selection screens return to Home. Back from a question pauses the active round on Home; Play then resumes it. Back from Results clears the completed round and returns Home.

# Repository Layout

- `app/` is the Android application module.
- `app/build.gradle.kts` configures SDK levels, Kotlin/Compose, dependencies, tests, release signing, and build types.
- `app/src/main/AndroidManifest.xml` declares TV support, launcher intents, banner, landscape orientation, and no touchscreen requirement.
- `app/src/main/assets/questions/core_questions.json` is the bundled question pack.
- `app/src/main/java/com/mdavis8403/magickingdomtrivia/data/` contains models, JSON parsing, repository abstractions, and DataStore repositories.
- `app/src/main/java/com/mdavis8403/magickingdomtrivia/domain/` contains settings, game state/engine, statistics accumulation, and state serialization.
- `app/src/main/java/com/mdavis8403/magickingdomtrivia/ui/` contains screen navigation, ViewModel, Compose UI, focus behavior, and theme.
- `app/src/main/res/` contains the app-name string, colors, Android theme, adaptive launcher art, and the 320 x 180 TV banner.
- `app/src/test/` contains local JVM tests for JSON parsing, filtering, game logic, statistics, and restoration serialization.
- `app/src/androidTest/` contains Android Compose tests for initial focus and D-pad navigation.
- `README.md` is the concise setup, build, install, question-update, and branding guide.
- `PROJECT.md` is the long-term source of truth.
- `signing.properties.example` documents local release signing without containing credentials.
- Root Gradle files configure plugin versions, repositories, and project-wide behavior.
- `.gitignore` excludes local SDK/signing files, keystores, IDE state, build output, and OS metadata.

# Question Database

## Storage and schema

The bundled pack is `app/src/main/assets/questions/core_questions.json`. It currently contains 104 original questions: 13 in each of eight categories. Easy has 40 questions, Medium has 32, and Hard has 32.

Each entry uses this schema:

```json
{
  "id": "animation_001",
  "question": "What kind of animal is Pascal in Tangled?",
  "answers": ["Chameleon", "Frog", "Gecko", "Iguana"],
  "correctAnswerIndex": 0,
  "category": "Disney Animation",
  "difficulty": "Easy",
  "explanation": "Pascal is Rapunzel's loyal pet chameleon.",
  "sourceTitle": "Tangled"
}
```

The required categories currently present are:

- Disney Animation.
- Pixar.
- Disney Princesses.
- Live Action Disney.
- Disney Parks.
- Disney Songs.
- Marvel.
- Star Wars.

Mixed is a synthetic selection that includes every valid category. Data categories are derived from JSON in first-seen order; adding a category does not require application-logic changes. Category IDs are normalized lowercase slugs.

## Validation

`QuestionJsonParser` validates each entry independently. A valid entry requires:

- A nonblank, unique, stable ID.
- Nonblank question text.
- Exactly four nonblank, distinct answers.
- A correct-answer index from 0 through 3.
- A nonblank category.
- Easy, Medium, or Hard difficulty.
- A nonblank explanation.
- A nonblank source title.

Malformed or invalid entries are skipped and logged with their entry index and reason. One invalid entry does not reject the whole pack. Invalid root JSON or a missing question array produces an empty catalog and a user-visible no-questions error rather than a crash.

## Selection and expansion

Question order can be randomized. Answer choices are converted to `TriviaChoice` objects and can be shuffled independently; correctness travels with the choice, so mapping remains valid after shuffling. A question ID cannot repeat within one game because selection starts from a unique filtered list.

To add questions, edit only the JSON asset, retain stable IDs, run local tests, and build the debug APK. The repository abstraction supports future bundled or imported packs, but only the core asset pack is currently loaded.

# User Experience

The interface uses a dark navy-to-plum gradient, jewel-tone gold/mint/rose accents, high contrast, large typography, generous spacing, large controls, subtle screen transitions, and 56 dp by 38 dp television-safe outer margins. Decorative artwork consists only of original geometry.

The experience should continue to resemble a polished family game show with a distinct original identity. Do not add Disney logos, character artwork, film stills, copyrighted music, trademark graphics as decoration, or other protected assets. Factual names may appear in questions and category labels.

The app name is sourced from `@string/app_name` for the manifest and Home screen, making it easy to rename in one place.

# Screens and Game Flow

1. **Home - implemented.** Shows Play/Resume, Categories, Difficulty, Game settings, Statistics, current category, difficulty, question count, and timer.
2. **Categories - implemented.** Shows Mixed plus all data-derived categories in a three-column remote-friendly grid.
3. **Difficulty - implemented.** Supports Easy, Medium, Hard, and Mixed.
4. **Game settings - implemented.** Supports question count, timer, randomization, explanations, automatic advancement, recent avoidance, and sound.
5. **Question - implemented.** Shows progress, score, category, difficulty, optional timer, prompt, and four choices in a 2 x 2 layout.
6. **Answer feedback - implemented inline.** Locks the answer, labels correct/incorrect choices in text and color, shows Correct/Incorrect/Time's Up, optionally explains, and focuses Continue.
7. **Results - implemented.** Shows message, score, percentage, correct/incorrect counts, best streak, best category, most difficult category, replay, and Home.
8. **Statistics - implemented.** Shows lifetime totals, accuracy, category/difficulty performance, high scores by game length, and a confirmed reset action.

Performance messages are editable in `TriviaGameEngine`:

- 90-100: Trivia Legend.
- 75-89: Disney Expert.
- 50-74: Rising Star.
- Below 50: Time for a Rewatch.

# Remote Navigation

Everything is operable with Up, Down, Left, Right, Center Select, and Back. Touch is not required.

Current focus behavior:

- Home initially focuses Play or Resume.
- Categories focuses the selected category.
- Difficulty focuses the selected difficulty.
- Settings focuses the first question-count choice.
- Statistics focuses Back to home.
- Each question focuses the first answer.
- After submission or timeout, focus moves to Continue/See results.
- Results focuses Play again.
- Confirmation dialogs focus Cancel to make destructive or consequential actions safe.
- TV Material controls provide focused styling; answer cards add a scale treatment.
- Focus requesters wait one rendered frame before requesting focus.

Back behavior:

- Selection/settings/statistics screens return Home.
- Question returns Home while preserving the active round; Play becomes Resume game.
- Results returns Home and clears the completed round.
- Dialog Back dismisses the dialog.
- Back on Home follows normal Android activity behavior.

Compose instrumentation tests verify Home initial focus and D-pad movement/selection into Categories. They compile locally but require an Android TV emulator or physical device to execute.

# Android TV Requirements

- Android TV/Leanback support is required in the manifest.
- Both `LAUNCHER` and `LEANBACK_LAUNCHER` are declared.
- The application has a 320 x 180 vector TV banner with original shapes and `MKT` lettering.
- Adaptive launcher icons use original vector geometry.
- Touchscreen hardware is explicitly not required.
- Android TV supplies a landscape display; the activity does not force an orientation, preserving Android 16 compatibility.
- System status/navigation bars are hidden with transient swipe behavior.
- Compose layouts fill available width and use scrolling where font scaling or smaller viewports require it.
- Root safe margins are 56 dp horizontal and 38 dp vertical.
- Minimum SDK is 26; compile and target SDK are 35.
- The code targets current Nvidia Shield TV software, but physical Shield verification remains required.

# Game Logic

Implemented behavior:

- Category and difficulty filtering.
- 10, 20, 30, or 50 requested questions.
- Use of every eligible question when fewer exist, after a confirmation notice.
- Optional random or stable question order.
- Optional random or stable answer order with preserved correctness.
- No duplicate question in one game.
- Recently played IDs are deprioritized when enough fresh questions exist; older questions fill shortages.
- One point per correct answer and zero per incorrect/timeout answer.
- Current and best streak tracking.
- Optional Off/10/15/30-second countdown.
- Timer expiration locks the question as an incorrect timed-out answer.
- Optional explanations.
- Manual Continue or optional automatic advancement after approximately three seconds.
- Results with integer accuracy, correct/incorrect totals, best streak, highest-performing category, and lowest-performing category.
- Replay with the same settings and updated recent history.

Ties in best/most-difficult category use the first category encountered in the round's grouped answers. Response time is not persisted and does not affect scoring.

# Settings and Persistence

Preferences DataStore persists:

- Category.
- Difficulty.
- Question count.
- Timer.
- Question-order randomization.
- Answer-order randomization.
- Explanations.
- Automatic advancement.
- Recently played avoidance.
- Sound effects.

Statistics persistence includes:

- Games played.
- Questions answered.
- Correct answers.
- Overall accuracy.
- Category answered/correct/accuracy.
- Difficulty answered/correct/accuracy.
- Highest score for each game length.
- The newest 200 unique recently played question IDs.

Statistics reset uses a confirmation dialog and removes only statistics/history, not game settings.

The active game state, including randomized questions/answers, score, progress, timer value, feedback state, and answers, is serialized into `SavedStateHandle`. It survives ordinary activity recreation and Android-supported process restoration. DataStore provides durable settings/statistics across launches.

# Sound

Sound effects are optional and enabled by default. The app uses Android `ToneGenerator` to create short local acknowledgement or error tones at runtime. No audio asset, copyrighted music, or network audio is used.

# Coding Standards

- Prefer readable code over clever code.
- Keep functions focused and names descriptive.
- Keep UI, navigation, game logic, loading, persistence, and statistics responsibilities separate.
- Use immutable state and pure domain transitions where practical.
- Avoid duplicate code and unnecessary frameworks.
- Do not add TODO/FIXME placeholders, pseudocode, or incomplete screens.
- Keep comments rare, useful, and concise.
- Handle malformed data and empty states safely.
- Invalid question data must not crash the app.
- Keep dependencies minimal and use stable releases compatible with the project toolchain.
- UI must never access raw question files or DataStore directly.

# Testing

There are 14 local JUnit tests covering:

- Valid JSON parsing.
- Mixed/data-driven category creation.
- Invalid-entry skipping and error reporting.
- Repository category and difficulty filtering.
- Persisted selection initialization.
- Filtered, duplicate-free game creation.
- Too-few-question notices.
- Answer shuffling and correct-answer preservation.
- Recently played avoidance.
- Scoring and detailed results.
- Timer expiration.
- Statistics totals, breakdowns, high scores, and recent history.
- Recent-history limits and newest-occurrence behavior.
- Active game-state serialization and corrupt-state rejection.

Two instrumented Compose tests cover initial Home focus and D-pad navigation/selection into Categories. `compileDebugAndroidTestKotlin` passes. The tests have not been executed because no emulator or Shield is connected in the current development environment.

`lintDebug` passes with zero errors. Its remaining warnings are dependency, Gradle, compile SDK, and target SDK version-availability notices retained for the verified AGP 8.13.2, Gradle 8.13, Kotlin 2.2.21, and SDK 35 compatibility set.

Still valuable to add:

- DataStore tests using a temporary on-disk store.
- ViewModel coroutine/navigation integration tests with injected repositories.
- More D-pad paths across Settings, Question feedback, Results, dialogs, and Back.
- Screenshot/layout checks at 1080p, 4K, and large font scales.
- Physical Shield soak and accessibility testing.

# Build and Installation

Prerequisites are JDK 17+ and Android SDK platform 35/build tools 35.0.0. Configure `ANDROID_HOME`/`ANDROID_SDK_ROOT` or an ignored `local.properties` file.

```bash
./gradlew test
./gradlew assembleDebug
./gradlew compileDebugAndroidTestKotlin
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Shield installation:

```bash
adb connect SHIELD_IP_ADDRESS:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Release builds use `./gradlew assembleRelease`. If ignored `signing.properties` exists, Gradle reads `storeFile`, `storePassword`, `keyAlias`, and `keyPassword` and signs the release. Otherwise it creates an unsigned release for verification. See `README.md` and `signing.properties.example`.

# Git Workflow

Never commit directly to `main`. Work on focused feature branches, run relevant tests and a debug build after each subsystem, use descriptive commit messages, push regularly, and open a pull request into `main`.

The active implementation branch is `codex/initial-build`. Future names should use the `codex/` prefix, such as:

- `codex/question-bank`
- `codex/settings`
- `codex/statistics`
- `codex/ui-polish`
- `codex/audio`

# Security and Repository Hygiene

- Never commit signing passwords, keystores, `signing.properties`, secrets, or tokens.
- Never commit `local.properties` or machine-specific SDK paths.
- Keep generated builds, caches, IDE state, captures, and OS metadata out of Git.
- Use only original or properly licensed visual/audio assets.
- Review staged changes before every commit.
- The application must remain free of unnecessary permissions, analytics, ads, and authentication.

# Roadmap

## Near term

- Run all instrumented focus tests on an Android TV emulator and Nvidia Shield.
- Verify every D-pad path, system Back behavior, overscan margin, font scaling, banner rendering, and immersive bars at 1080p and 4K.
- Add broader Compose focus and screenshot tests.
- Add ViewModel and temporary-DataStore integration tests.
- Fact-check and editorially review the entire question pack with a second human reviewer.
- Add GitHub Actions for unit tests and debug APK artifacts.

## Medium term

- Support multiple bundled packs and safe imported custom packs.
- Expand to 500+ balanced questions with source review.
- Add local player profiles, multiplayer/team play, and achievements.
- Add richer original sound design, music, and optional reduced-motion/accessibility settings.
- Add more result breakdowns and question-history controls.

## Long term

- Add deterministic offline daily challenges.
- Establish a versioned community question-pack format and import validator.
- Explore optional leaderboards only with an explicit privacy/security design that preserves offline play as the default.
- Add seasonal original themes without protected artwork or music.

# Known Limitations

- The 104 questions provide only 13 questions in each specific category; 20/30/50-question category rounds therefore show a shortage notice and use all eligible questions. Mixed supports larger rounds.
- Only one bundled question pack is loaded; custom pack importing is not implemented.
- Questions are validated at runtime rather than by a dedicated Gradle validation task.
- Question content has not received independent editorial/fact-check review.
- Response time is not recorded, and timer speed does not change scoring.
- Best/most-difficult category tie handling is simple first-encounter order.
- The active game uses SavedStateHandle, not durable resume after a user force-stops/clears the task or clears app data.
- Instrumented UI tests compile but have not been run in this environment.
- Nvidia Shield hardware, 1080p/4K layouts, overscan, accessibility services, and large font scales remain unverified on device.
- Generated tones and banner/launcher artwork are intentionally simple.
- There is no CI workflow, app bundle publishing, store metadata, or committed release key.

# Definition of Done

A feature is complete only when:

- The project builds successfully.
- Relevant tests pass.
- It works using only the Shield remote.
- Focus remains visible and predictable.
- The UI is polished and safe at television viewing distance.
- No unfinished TODOs, placeholder functions, or dead code remain.
- Error and empty states are handled safely.
- `PROJECT.md` reflects architecture and behavior changes.
- `README.md` reflects build, install, question, or branding workflow changes.
- The feature is committed on a feature branch.
- The feature is reviewed through a pull request before merge.

# Maintenance Instructions for Future Codex Tasks

1. Read `PROJECT.md` completely before making changes.
2. Inspect the current repository before assuming architecture.
3. Work on a feature branch and never commit directly to `main`.
4. Keep changes focused and reuse working code.
5. Run relevant tests after each subsystem.
6. Attempt a debug build after each subsystem.
7. Update `PROJECT.md` when architecture, behavior, dependencies, or workflows change.
8. Update `README.md` when build, installation, question, signing, or branding instructions change.
9. Report limitations, failed checks, and unverified device behavior honestly.
10. Never commit directly to `main`.
