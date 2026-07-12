# Magic Kingdom Trivia

Magic Kingdom Trivia is an offline native Android TV trivia experience built with Kotlin and Jetpack Compose for TV, with the Nvidia Shield remote as its primary input.

Read [PROJECT.md](PROJECT.md) before making changes. It is the source of truth for the implemented architecture, current limitations, standards, and roadmap.

## Highlights

- D-pad friendly category selection and answer flow
- Curated Magic Kingdom question bank with themed categories
- Score tracking, streaks, and end-of-round recap
- Pure Kotlin game engine with unit tests

## Build

1. Install JDK 17 or newer and Android SDK platform 35 with build tools 35.0.0.
2. Set `ANDROID_HOME`/`ANDROID_SDK_ROOT`, or create an untracked `local.properties` file with `sdk.dir=/path/to/Android/sdk`.
3. Run `./gradlew test`.
4. Run `./gradlew assembleDebug`.

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Install on Nvidia Shield

Enable developer options and network debugging on the Shield, then run:

```bash
adb connect SHIELD_IP_ADDRESS:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
