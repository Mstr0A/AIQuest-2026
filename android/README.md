# DALEELAK Android skeleton

Native Android, Kotlin and Jetpack Compose, using the generated IntelliJ project.
The current app is Arabic-first and uses the selected fixed Navy and Sky Material theme. Home and voice-first chat controls are implemented; remaining UI evolves with the user's direction.

## Implemented skeleton

- Home entry point with new operation/chat, all history, current operations and finished operations; Locations remains accessible.
- Fixed Navy and Sky light theme, native vector icons, RTL layout and Android back handling.
- Voice-first chat controls with readable messages and optional keyboard typing. Input/output audio remain labeled placeholders.
- Editable typed composer, exactly three suggested prompts, and scripted demo clarification.
- Save/resume operations; new/ongoing/completed filters; per-step progress and a dependency stepper.
- Versioned on-device persistence with plan snapshots, checklist selections, answers and labeled demo events.
- Local simulated booking, completion/reopening, and confirmed deletion.
- ViewModel ownership of state, independent domain types, local catalog boundary, assistant/transcription interfaces.

## Project layout

```text
app/src/main/java/com/a0/daleelak/
  app/                    app shell and ViewModel
  domain/                 models and integration interfaces
  data/                   illustrative catalog and local persistence
  features/assistant/     messages and editable composer
  features/journey/       stepper and requirements
  features/operations/    history and filters
  features/locations/     location catalog empty state
  ui/theme/               existing generated Material theme
```

## Next integration work

Read [the skeleton review and work split](../docs/daleelak/work-plan.md) before parallel changes. UI ownership and assistant/state ownership are separate; coordinate the additive ViewModel interface.

The conversation and plan are **illustrative layout fixtures, not official procedural guidance**.
The microphone control currently explains the pending adapter; it does not record or transcribe.
The listening control also explains pending playback; no spoken replies are generated yet.
The location catalog is intentionally empty pending verified entries. No invented centre, hours, map links or distances.
No AI/network integration, credentials, live bookings, submissions, accounts or government status access.

Connect an AI provider via a credential-safe backend using `../docs/daleelak/ai-response-contract.md` and its schema.
The simplified domain models are UI skeleton models, not a replacement for that wire contract. Add DTO parsing,
shape/semantic/source validation and plan reconciliation before accepting generated procedural plans.
Add source-reviewed branch records and a verified place catalog; compute relevance/distance in the app.
Add short audio recording/transcription to the editable composer, with permission/error handling and typed fallback.
Persist resumable chat context once real conversation orchestration is added; this version retains answers and plan,
but the demonstration chat itself starts fresh after process recreation.

Open this folder in IntelliJ/Android Studio, sync Gradle, and run the `app` configuration on an API 26+ device.
Generated Gradle wrapper scripts were absent in the supplied project; use the IDE's configured Gradle distribution.
Aligned the Compose compiler plugin with AGP's Kotlin 2.2.10 and removed references to missing launcher icons.
Automatic cloud backup is disabled so operation history stays device-local.
Canonical product documents are in `../docs/daleelak`; this Android folder does not duplicate them.
No implementation tests were added or run, following the contributor instructions. Debug assembly is a build check.

### Verification in this session

Debug assembly reached resource generation and `compileDebugKotlin`, then stopped because the offline Gradle
cache is missing Kotlin 2.2.10 compiler/build-tools JARs and related dependencies. Compilation and APK output
are therefore **not verified**. Sync Gradle with network access in the IDE, then build/run `app`.
This session needed task-local `JAVA_OPTS=-Duser.home=C:\Users\damar`, `GRADLE_USER_HOME=C:\Users\damar\.gradle`
and `ANDROID_USER_HOME` pointing to the project's ignored `.android` directory. These are sandbox workarounds,
not required project environment settings for the normal desktop user.

## Coordination record — 2026-10-09

The user authorized beginning UI implementation, starting with home navigation, then requested voice-first chat with optional typing and readable text. This agent changed only UI files and documentation. No ViewModel/domain/data or build wiring was changed. Java and Gradle executables are unavailable in this shell, so this increment was not compiled or run; no tests were added or run.

The user authorized the Android skeleton and selected native Android after dropping KMP.
Original project path: `C:\Users\damar\IdeaProjects\Daleelak`. Repository project path: `android/`.
The Android project and canonical docs now share this repository.
Another agent is working on this repository. The user authorized copying the project into `android/` and pushing the current branch.
Remote pulls were attempted but this tool session cannot connect to GitHub; no claim of remote synchronization.
Preserve the Society/Jordan scope and lost-family-book flagship. Booking remains labeled local simulation.
