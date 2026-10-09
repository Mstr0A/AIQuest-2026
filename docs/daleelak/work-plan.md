# Skeleton review and parallel work plan

Recorded 2026-10-09 after fetching Android skeleton commit `9567827`. Local merge `85cc601` preserves that skeleton and the Android/Kotlin skills commit `8d9328b`. This is a code inspection and proposed work assignment; no implementation, build, or tests were run for this review.

## What exists

**UI update, 2026-10-09:** the user authorized implementation. Home navigation, operation filters, fixed Navy and Sky theme and voice-first chat controls are now implemented. Audio capture/transcription/playback remain pending; UI controls disclose that status. The snapshot below describes the original skeleton before this increment. UI-only home routing preserves the other agent's existing destination enum and ViewModel methods.

- Native Android, Kotlin, Jetpack Compose, Material 3, package `com.a0.daleelak`.
- Assistant, Operations and Locations destinations; Arabic-first RTL shell.
- Local operation snapshots, checklist selections, dependency-aware step completion, history filters, completion/reopening and deletion.
- Booking button that creates a labeled local demo event without confirming official progress.
- Three static prompts and two scripted clarification turns. No AI connection; the script does not inspect the meaning of the user's issue.
- A three-step layout fixture in `DemoCatalog`, not reviewed government guidance. Locations are empty; dictation is a notice only.
- Template purple theme with dynamic color enabled. Navy and Sky is documented but not implemented.

## Main gaps

1. Real source-grounded conversation and validated plans are the core missing capability. `AssistantGateway` currently returns only `String`; the complete JSON wire contract needs separate DTOs, validation and mapping to the UI domain.
2. `AssistantScreen` directly reads `DemoCatalog.plan` and `DemoCatalog.prompts`. The UI must consume the accepted plan and contextual prompts supplied by the ViewModel before a real response can be shown.
3. Journey rows currently flatten requirements. They need separate checklist categories, visible known formats and expandable source detail.
4. Reviewed place records, relevance filtering and app-calculated distance remain missing. No live availability is established.
5. Saved operations contain answers and plan snapshots, but chat context does not survive process recreation. Replanning must preserve unaffected progress.
6. Manifest has neither internet nor microphone permissions. Recording, transcription and provider setup are pending.
7. README records an unsuccessful earlier build. Wrapper properties exist, but wrapper scripts/JAR are absent. This review does not establish compilation or APK output.

## Selected work split

Paths below are relative to `android/app/src/main/java/com/a0/daleelak/` unless stated otherwise. File ownership avoids simultaneous edits; method additions are coordinated through the interface below.

| Owner | Task | Files owned |
| --- | --- | --- |
| This agent with Eyas | Minimalist Arabic UI and Navy and Sky theme, using the user's forthcoming layout vision | `ui/**`, `features/**`, `app/DaleelakApp.kt`; new UI components under `ui/` |
| Other agent | Source-grounded assistant, validated responses and operation state | `app/DaleelakViewModel.kt`, `domain/**`, `data/**`; new `ai/**`; `MainActivity.kt`; source assets; build/manifest wiring |

The other agent owns dependencies and permissions. If UI needs an icon library or resources outside its ownership, request that change through the other agent. Neither agent edits the other's files. Changes to this plan or the canonical schema require coordination. No agent is automatically spawned or contacted by recording this assignment.

### Shared interface proposal

Preserve existing public ViewModel properties/actions and domain field names used by the screens. Add fields with defaults where possible; do not break saved snapshots without migration.

The other agent adds these observable ViewModel properties, retaining the existing `send`, `savePlan`, `newConversation` and progress actions:

```kotlin
val currentPlan: GuidancePlan? // Latest accepted plan; null until established.
val suggestedPrompts: List<String> // Exactly three; contextual when available.
val isResponding: Boolean
```

These are proposed properties, not present in the inspected skeleton. Update the UI to read them after the other agent supplies them. Keep `showPlan` compatible with `currentPlan != null`. `savePlan()` saves the accepted plan, never an unconditional `DemoCatalog.plan`. Preserve `messages`, `draft` and `notice` so the composer remains editable and failure feedback stays available. `illustrative = false` requires source-reviewed content and validation, not merely successful JSON parsing.

The UI groups `Requirement.category`, shows `Requirement.format` only when known, and exposes `sourceIds`. The other agent supplies resolved source/place records through an agreed catalog accessor; the UI never builds invented records. Additional source, question, place or dictation state should be agreed in this document before both sides consume it.

## Order of work

1. Share this assignment and agree to the additive ViewModel interface. Keep the existing demo runnable while provider setup is unresolved.
2. This agent implements Navy and Sky theme roles and UI components following Eyas's incremental UI vision. The user now specifies home as the entry point, with new chat and all/current/finished operation links. Chat is voice first, with optional keyboard and visible text. Spoken replies are intended by default after audio integration. Dynamic colors stay disabled.
3. Other agent prepares reviewed pilot source records, JSON DTO/parser/validator and structured assistant state. Preserve the distinction between unsupported coverage and transport failure. Source-supported clarification only; no general profiling.
4. Other agent connects the selected AI provider through a credential-safe arrangement. Provider/model and hosting remain unchosen; do not put secrets in the APK or assume a new paid service is authorized.
5. Integrate accepted plans, save/resume context and replan reconciliation. Then connect verified places and editable dictation. Keep booking as labeled local simulation.
6. Build/run verification is still pending. Do not add or run implementation tests without a user request.

Core judge journey: describe the lost book, clarify a documented branch, show the sourced plan and categorized requirements, save/resume, mark user-reported progress, and invoke the clearly labeled booking simulation. Places and voice remain required demo features; the order above is sequencing, not removal.

## Copyable task for the other agent

> Pull the latest `docs/daleelak-context` branch and read `android/AGENTS.md` plus `docs/daleelak/work-plan.md`. You own the assistant/state layer: `app/DaleelakViewModel.kt`, `domain/**`, `data/**`, new `ai/**`, source assets, `MainActivity.kt`, build files and manifest. Our agent owns `features/**`, `ui/**` and `app/DaleelakApp.kt`; do not edit those. Replace the fixed-stage chat with issue-focused, source-grounded guidance for the lost-family-book pilot. Use only supplied reviewed documents, the existing JSON contract and source/graph/ID validation. Start with reviewed source records and typed response handling while provider setup remains unresolved. Add observable `currentPlan`, `suggestedPrompts` (exactly three) and `isResponding`, preserving existing screen-facing actions. Save accepted plans and resumable context; preserve unaffected progress when answers change. Missing rules stay explicit. No invented portrait, fees, places or official outcomes. Keep booking simulation labeled. Preserve local-only operation storage and editable transcription fallback. Coordinate additional source/place/voice accessors before UI integration. Commit only your owned files and preserve others' work. Do not add or run implementation tests without a user request.
