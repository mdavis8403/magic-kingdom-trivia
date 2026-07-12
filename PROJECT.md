# Project Overview

Magic Kingdom Trivia is a native Android TV trivia game written for the Nvidia Shield TV and operated with the Shield remote. The goal is a polished, premium living-room experience rather than a mobile interface enlarged for a television. Gameplay is entirely offline; the current question bank is compiled into the application and the app does not request network access.

The current implementation is an initial playable build. It offers category selection, five-question rounds, immediate answer feedback, scoring, streak tracking, and a results screen.

# Core Principles

Development should prioritize:

- Fast performance.
- Clean architecture.
- Family-friendly design and content.
- A beautiful television interface.
- Extremely reliable remote navigation.
- Easy expansion of the question database.
- Maintainability over cleverness.

Avoid unnecessary complexity. Add libraries and architectural layers only when they solve a demonstrated project need.

# Technology Stack

The repository currently uses:

- **Kotlin 2.2.21** for application and game logic.
- **Android Gradle Plugin 8.7.3** and **Gradle 8.10.2** through the checked-in Gradle wrapper.
- **Jetpack Compose** for all UI.
- **Compose for TV**, specifically `androidx.tv:tv-foundation:1.0.0` and `androidx.tv:tv-material:1.1.0`.
- **TV Material 3** components from `androidx.tv.material3`. The app does not use the phone/tablet Material 3 dependency.
- **AndroidX Activity and Lifecycle ViewModel** integration.
- **JUnit 4** for local unit tests.
- **Git** for version control and GitHub for remote collaboration.
- **Android Studio** as the intended IDE, although it is not encoded as a project dependency.

The project does **not** currently use local JSON data, DataStore, a database, dependency injection, a navigation library, or any other persistence mechanism. All question data is Kotlin source code and all state is held in memory.

# Architecture

The current application has a small, layered structure:

1. `MainActivity` creates the Compose content and obtains `TriviaViewModel` through the AndroidX `viewModel()` helper.
2. `TriviaApp` and its private screen composables render immutable snapshots of `TriviaGameState` and forward user actions to the ViewModel.
3. `TriviaViewModel` owns the current in-memory UI state and delegates every state transition to `TriviaGameEngine`.
4. `TriviaGameEngine` contains category selection, round creation, answer submission, progression, scoring, streak, replay, and summary logic.
5. `TriviaRepository` owns the compiled category and question lists and exposes category and question lookup methods.

Navigation is not implemented with Navigation Compose. `TriviaApp` derives one of three internal screens from whether `TriviaGameState` contains an active session or a completed summary, and `AnimatedContent` transitions between those screens.

The UI does not directly access raw question data. It receives categories and questions through state created by the engine and repository. There are no raw question files yet.

Settings persistence and statistics persistence do not exist. The current ViewModel does not use `SavedStateHandle`, so game state is not restored after activity or process recreation. Persistence belongs in future repository/data-source layers rather than in composables.

# Repository Layout

- `app/` is the Android application module.
- `app/build.gradle.kts` configures Android SDK levels, Java/Kotlin 17, Compose, dependencies, and build types.
- `app/src/main/java/com/mdavis8403/magickingdomtrivia/` contains the activity and Kotlin source.
- `app/src/main/java/com/mdavis8403/magickingdomtrivia/data/` contains the trivia models and the in-code repository/question bank.
- `app/src/main/java/com/mdavis8403/magickingdomtrivia/domain/` contains game state models and `TriviaGameEngine`.
- `app/src/main/java/com/mdavis8403/magickingdomtrivia/ui/` contains the ViewModel, Compose screens, and theme wrapper.
- `app/src/main/res/` contains app strings, colors, the Android theme, adaptive launcher icons, and the TV banner drawable.
- `app/src/test/` contains local JVM tests for the game engine.
- `app/src/main/AndroidManifest.xml` declares Android TV, launcher, orientation, banner, and touchscreen requirements.
- `README.md` is the short onboarding, build, and installation guide.
- `PROJECT.md` is the long-term source of truth for current architecture, behavior, standards, and roadmap.
- Root `build.gradle.kts`, `settings.gradle.kts`, and `gradle.properties` configure plugins, repositories, the root project, and Gradle behavior.
- `gradle/wrapper/`, `gradlew`, and `gradlew.bat` provide Gradle 8.10.2 without requiring a system Gradle installation.
- `.gitignore` excludes IDE state, local SDK configuration, captures, and generated build outputs.

There is currently no JSON question directory or file. The launcher foreground and banner are original vector/XML resources. The banner is an early abstract placeholder and has not been visually verified on Shield launchers.

# Question Database

## Current storage

The current bank is defined directly in `TriviaRepository.kt`. It contains 25 questions: five questions in each of five categories (`lands`, `rides`, `characters`, `food`, and `history`). Each round therefore currently uses every question in its selected category, although their order is randomized.

The actual Kotlin question shape is:

```kotlin
data class TriviaQuestion(
    val id: String,
    val categoryId: String,
    val prompt: String,
    val choices: List<TriviaChoice>,
    val explanation: String,
    val difficulty: Difficulty,
)

data class TriviaChoice(
    val text: String,
    val isCorrect: Boolean,
)
```

IDs such as `rides_3` are intended to remain stable. Each current question has prompt text, exactly four choices, one choice marked with `isCorrect = true`, a category ID, an `EASY`, `MEDIUM`, or `HARD` difficulty, and an explanation. There is no source title field.

## JSON schema status

No JSON is loaded or parsed, so there is no current JSON schema in production. The following is the **planned** schema that preserves the current model while adding the requested source metadata and a less error-prone correct-answer mapping:

```json
{
  "packId": "core-v1",
  "questions": [
    {
      "id": "rides_3",
      "question": "Question text",
      "answers": ["Answer A", "Answer B", "Answer C", "Answer D"],
      "correctAnswerIndex": 0,
      "category": "rides",
      "difficulty": "medium",
      "explanation": "Why the answer is correct.",
      "sourceTitle": "Source name"
    }
  ]
}
```

Before JSON packs are accepted, a loader should validate:

- Pack IDs and question IDs are nonblank and unique.
- Question text and explanation are nonblank.
- Every question has exactly four nonblank, distinct answers.
- `correctAnswerIndex` is between 0 and 3.
- Category refers to a known category or a category declared by the pack.
- Difficulty is one of the supported values.
- Source title is nonblank when source metadata is required.

Validation is not currently implemented. Because current entries are Kotlin constructors, malformed types fail compilation, but semantic errors such as duplicate IDs, multiple correct choices, no correct choice, or unknown category IDs are not checked. Repository lookup can throw when a category is missing. Future loaders must reject or skip invalid entries safely, report diagnostics, and avoid crashing the app. A pack with no valid questions must produce a user-visible error state.

Question order is shuffled by the engine. Answer choices are **not** shuffled, and the current correct answer remains identified by the `isCorrect` Boolean on each choice. New questions currently require editing and recompiling `TriviaRepository.kt`; multiple loadable packs without application-logic changes are a roadmap goal.

# User Experience

The intended experience is a polished family game show with Disney-inspired magic and a distinct original identity. It should use large text, large answer cards, elegant transitions, fast feedback, clear hierarchy, minimal clutter, television-safe margins, and strong focus states.

The current UI uses a dark navy/blue/plum gradient, generous 48 dp horizontal and 32 dp vertical outer padding, large typography, TV Material components, category accent colors, and an animated transition between major screens. Feedback appears immediately after an answer, followed by an explanation.

Do not add Disney logos, character artwork, movie stills, copyrighted music, or other protected assets. Original visual motifs, animation, illustration, and audio are required.

# Screens and Game Flow

1. **Home screen - implemented.** Shows the title, selected round/category, start button, and a horizontal row of five category cards.
2. **Game setup - partially implemented on Home.** Category selection exists. Question count, difficulty, timer, and other options do not.
3. **Question screen - implemented.** Shows category, progress, score, difficulty label, prompt, and four answer buttons.
4. **Answer feedback - implemented inline.** Locks answers, labels the correct answer and the player's incorrect selection when applicable, displays correctness text and the explanation, and shows a Next/Results button.
5. **Results screen - implemented.** Shows correct answers, total questions, integer accuracy percentage, category, best streak, replay, and return-home actions.
6. **Statistics - planned.** No screen or persisted statistics exist.
7. **Settings - planned.** No screen or persisted settings exist.

Rounds are fixed at five questions and filtered only by the selected category. Difficulty is displayed as `Warm-up`, `Challenge`, or `Deep cut` but cannot be selected or filtered. There is no timer. Explanations are always shown after an answer. Advancement always requires selecting the Next button.

# Remote Navigation

Every current and future interaction must be operable with only Up, Down, Left, Right, Center Select, and Back. Touch input must never be required.

Required behavior:

- Focus never disappears.
- Every screen has a logical default focus.
- Focus is clearly visible at television viewing distance.
- Back behaves predictably.
- Navigation order is intentional.
- Focus is restored appropriately when returning to a screen.

The current app uses focusable TV Material `Button`, `OutlinedButton`, and `Card` components. The Home screen explicitly requests focus for the Start button, and category cards define a 3 dp accent-colored focused border. Other controls rely on TV Material's default focus styling and Compose's automatic spatial navigation.

Known focus limitations are significant: the Question and Results screens do not explicitly request default focus; focus restoration is not implemented; custom directional ordering is not defined; and after an answer all answer buttons become disabled while the newly displayed Next button has no explicit focus request. Back handling is also not customized, so Android's default activity behavior applies instead of navigating through game screens. These behaviors require device-level testing and hardening before remote navigation can be considered extremely reliable.

# Android TV Requirements

Current configuration and status:

- **Android TV launcher support - implemented.** The manifest declares `android.software.leanback` as required.
- **Leanback launcher intent - implemented.** `LEANBACK_LAUNCHER` and standard `LAUNCHER` categories are declared.
- **Television banner asset - present.** `@drawable/tv_banner` is assigned to the application, but the placeholder asset has not been verified on an Nvidia Shield.
- **No touchscreen requirement - implemented.** `android.hardware.touchscreen` is declared with `required="false"`.
- **D-pad navigation - partially implemented.** TV Material controls are focusable, but focus transitions and Back behavior need hardening and device tests.
- **Landscape gameplay - implemented.** `MainActivity` requests landscape orientation.
- **Full-screen gameplay - partial.** The app uses an edge-to-edge, no-action-bar activity; it does not explicitly hide system bars or enable an immersive mode.
- **1080p and 4K compatibility - intended, not verified.** Compose uses responsive width and scroll containers, but no emulator/device matrix or screenshot tests currently verify these resolutions.
- **Television safe margins - implemented at the root.** Content receives 48 dp horizontal and 32 dp vertical padding; overscan behavior has not been tested on hardware.
- **SDK versions - configured.** Minimum SDK is 26, compile SDK is 35, and target SDK is 35.
- **Nvidia Shield compatibility - targeted, not verified on hardware.** Minimum SDK and TV manifest declarations are compatible in principle, but no Shield installation or remote test is documented yet.

# Game Logic

Implemented behavior:

- The first repository category is selected initially.
- Starting a game filters questions by the selected category.
- Eligible questions are shuffled, then up to five are selected without duplicates.
- The round size is fixed at five in production.
- Each correct answer adds one point; incorrect answers add zero.
- Current and best correct-answer streaks are tracked.
- An answer can be submitted only once per question.
- The correct answer is found from the shuffled question's choices using `isCorrect`; choices themselves are not shuffled.
- Next is blocked until the current question has been answered.
- After the last answer, Next creates a summary and ends the active session.
- Replay starts a newly shuffled round in the same category.
- Return Home clears the current summary.
- If fewer than five eligible questions exist, all available questions are used.

Not implemented:

- Difficulty filtering.
- Configurable question counts.
- Answer-order randomization.
- Timers or time-based scoring.
- Recently played avoidance across rounds.
- State restoration after configuration change, process death, or app restart.
- A safe empty-question state. A category with zero eligible questions would create an unusable session and can lead to an index or division error.
- Validation of category selection or answer indices. UI-generated indices are valid, but malformed direct calls are not guarded.

# Settings and Persistence

There is no persistence dependency or local persistence code. All settings and game data reset when the ViewModel is recreated.

Current settings behavior:

- Question count: fixed at five.
- Category: selectable for the current in-memory session.
- Difficulty: displayed per question, not selectable.
- Timer: absent.
- Question randomization: always enabled.
- Answer randomization: absent.
- Explanations: always enabled.
- Automatic advancement: absent; manual advancement is required.
- Sound effects: absent.
- Recently played question avoidance: absent.

Planned persistence should cover question count, category, difficulty, timer, randomization, explanations, automatic advancement, sound effects, and recently played avoidance. DataStore is a reasonable intended direction for small settings, but it is not yet selected or installed.

Statistics persistence is entirely planned. It should eventually record games played, questions answered, correct answers, overall accuracy, category performance, difficulty performance, high scores, and recently played question IDs. Storage design and retention limits must be decided before implementation.

# Coding Standards

- Prefer readable code over clever code.
- Keep functions focused and use descriptive names.
- Keep UI rendering separate from business logic and data loading.
- Use immutable UI state where practical.
- Avoid duplicate code and unnecessary frameworks.
- Do not introduce TODO placeholders or unfinished implementations.
- Keep comments useful, concise, and focused on non-obvious decisions.
- Handle errors safely and provide useful user-visible states.
- Invalid question data must never crash the app.
- Keep dependencies minimal and justify new ones.
- Keep raw question loading inside data/repository code; UI must consume validated models.

# Testing

`TriviaGameEngineTest` currently contains four local JUnit tests:

- Initial state selects the first category.
- Starting a game creates a three-question test round for the selected category.
- Correct answers update score and produce a complete summary with accuracy and streak.
- An incorrect answer breaks the current streak while preserving the best streak.

These tests cover basic category filtering, game creation, score calculation, progression, summary creation, and streak behavior. They use a seeded `Random`, but do not directly prove question-order shuffling. The test suite passed during the initial implementation.

Tests still needed:

- JSON loading and schema validation after JSON is introduced.
- Invalid-entry handling and empty/undersized question banks.
- Difficulty filtering.
- Explicit question-order randomization and duplicate prevention.
- Answer shuffling and correct-answer preservation after shuffling.
- Recently played avoidance.
- ViewModel state transitions and restoration.
- Statistics and settings persistence.
- Compose UI and remote focus navigation, including default focus, focus after feedback, Back, and focus restoration.
- 1080p/4K layout and accessibility checks.

# Build and Installation

Prerequisites are JDK 17 or newer and an Android SDK containing platform 35 and build tools 35.0.0. Android Studio can provision these components. Set `ANDROID_HOME`/`ANDROID_SDK_ROOT`, or create an untracked `local.properties` file containing:

```properties
sdk.dir=/path/to/Android/sdk
```

Run all local unit tests:

```bash
./gradlew test
```

Build the debug APK:

```bash
./gradlew assembleDebug
```

The expected APK is:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To install over a network-connected Nvidia Shield with developer options and network debugging enabled:

```bash
adb connect SHIELD_IP_ADDRESS:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Approve the debugging connection on the Shield when prompted. Do not commit `local.properties` or any machine-specific SDK path.

# Git Workflow

Never commit directly to `main`. Create focused feature branches, use descriptive commit messages, push the branch, and open a pull request into `main` for review.

The active initial implementation branch is `codex/initial-build`. Current and suggested future branch names use the `codex/` prefix, for example:

- `codex/question-bank`
- `codex/settings`
- `codex/statistics`
- `codex/ui-polish`
- `codex/audio`

# Security and Repository Hygiene

- Do not commit signing passwords, keystore files, tokens, or other secrets.
- Do not commit `local.properties` or machine-specific SDK paths.
- Keep generated build outputs, IDE state, captures, and local caches out of Git.
- Maintain `.gitignore` as tools and build workflows evolve.
- Review staged changes before every commit.
- If release signing is added, source credentials from a secure local or CI secret store.

# Roadmap

## Near term

- Move questions to validated local JSON packs with stable IDs and source metadata.
- Add loader, validation, malformed-entry handling, and question-bank tests.
- Expand and fact-check the question bank; balance categories and difficulty.
- Add a real setup screen for question count, category, difficulty, timer, and explanation options.
- Harden default focus, focus transfer after answers, Back behavior, and focus restoration.
- Test installation, layout, and remote behavior on an Nvidia Shield at 1080p and 4K.
- Add settings persistence and game-state restoration.
- Replace or refine placeholder launcher/banner assets and continue UI/accessibility polish.
- Add GitHub Actions for tests and debug APK builds.

## Medium term

- Support multiple bundled question packs and importable custom packs.
- Add recently played avoidance and persistent statistics.
- Add statistics and achievement screens.
- Add original sound effects, game-show music, animated backgrounds, and seasonal themes with user controls.
- Add accessibility options for text size, motion, color contrast, audio, and timing.
- Add local multiplayer and team play.

## Long term

- Add daily challenges without compromising the offline-first core; bundled deterministic challenges are preferred.
- Add richer achievements and local player profiles.
- Explore optional leaderboards only with an explicit privacy, security, and online-mode design.
- Establish a versioned community question-pack format and safe import workflow.

# Known Limitations

- Only 25 compiled-in questions exist, five per category.
- Questions are Kotlin source rather than loadable JSON; there is no source-title metadata or validation layer.
- Every category round currently contains the same five questions in randomized order.
- Answers are not shuffled.
- Difficulty cannot be filtered, and question count cannot be changed.
- Settings, statistics, achievements, audio, multiplayer, timers, and recently played avoidance are absent.
- State is memory-only and is not restored after ViewModel/process recreation.
- Empty question sets and malformed repository data are not handled safely.
- Only Home, Question/feedback, and Results are implemented; setup is embedded in Home.
- Focus is explicitly initialized only on Home. Feedback-to-Next focus, Results default focus, restoration, and directional order are not controlled.
- Back navigation uses default Android behavior and is not game-aware.
- Full immersive system-bar behavior is not implemented.
- The theme uses TV Material defaults rather than a complete custom design system.
- The banner and launcher art are basic original vector placeholders.
- No instrumentation, Compose UI, screenshot, accessibility, or remote-navigation tests exist.
- Nvidia Shield installation, D-pad behavior, 1080p/4K rendering, safe margins, and banner appearance have not been verified on physical hardware.
- There is no CI workflow or release/signing configuration.

# Definition of Done

A feature is complete only when:

- The project builds successfully.
- Relevant tests pass.
- The feature works using only the Shield remote.
- Focus remains visible and predictable.
- The UI is polished and consistent with the living-room experience.
- No unfinished TODOs or placeholder implementations remain in the feature.
- Error and empty states are handled safely.
- `PROJECT.md` is updated when architecture, behavior, or workflow changes.
- `README.md` is updated when onboarding, build, or installation changes.
- The feature is committed to Git on a feature branch.
- The feature is reviewed through a pull request before merging.

# Maintenance Instructions for Future Codex Tasks

1. Read `PROJECT.md` before making changes.
2. Inspect the current repository before assuming its architecture.
3. Work on a feature branch and never commit directly to `main`.
4. Keep changes focused on the requested outcome.
5. Run all relevant tests.
6. Attempt a debug build.
7. Update `PROJECT.md` when architecture, behavior, dependencies, or workflows change.
8. Update `README.md` when build or installation steps change.
9. Report limitations, failed checks, and unverified device behavior honestly.
10. Never commit directly to `main`.
