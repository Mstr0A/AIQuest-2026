# Skeleton review and parallel work plan

## Current parallel batch — 2026-10-09, U-041

User requests multiple explicitly separate atomic/larger worker tasks and building/running
the app; U-042 selects a real phone over an emulator. Use two OpenRouter Qwen workers,
not Codex-native model slots. Each proposes edits; only the orchestrator applies them.

| Owner | Task | Exact writable file |
| --- | --- | --- |
| Worker A, task `chat-state` | Replace fixture reads with accepted plan/contextual prompts; show busy state and documented gaps | `features/assistant/AssistantScreen.kt` |
| Worker B, task `journey-details` | Show sourced summary/uncertainties and conditional/helpful checklist metadata while preserving snapping/dependencies/accessibility | `features/journey/JourneyScreen.kt` |
| Orchestrator | Review both diffs/baselines, integrate, build APK and install/launch on the selected phone | Worker tooling/docs; accepted edits to the two files above |
| Existing external agent | Owns assistant/state/provider and build/manifest source changes | Existing ownership below; no new external messages sent |

Freeze the current public ViewModel/domain interface during this batch. Workers may
read explicit context files but cannot change them, common components, build files,
manifest, schema, source data or each other's screen. Shared changes go through the
orchestrator after both tasks finish. This prevents same-file edits, not all possible
behavioral integration problems; review/build still required. No third worker needed
before verified place/source/voice interfaces exist. Keep each task to one reviewable
screen change, up to two calls; share the existing local $1 reservation ledger.

Task packets: `tools/coding-worker/chat-state.json` and `journey-details.json`.
The runner now locks only ledger updates, allowing independent HTTP requests to
overlap without duplicating/reseting spending limits. No worker writes live code.

### Batch result

Both requests completed concurrently, with one shared ledger: Chat $0.0019834848
(6,896 input / 1,470 output tokens); Journey $0.0006916932 (4,469 / 203).
Batch total $0.002675178; all three live calls total $0.0039155688.
Local reservations total $0.15, leaving $0.85; billed costs are separate.

Chat's proposal partially satisfied the task but used `!!`, omitted busy feedback,
did not disable Save and blocked the typing toggle while busy. The orchestrator
used a nullable immutable plan snapshot, completed busy/save behavior and retained
editing. Journey returned no edits, incorrectly interpreting the task as forbidding
requested UI additions. The orchestrator completed its screen changes locally,
and clarified the worker instruction to allow task-requested UI changes. No paid
retries. Both final screens preserve dependencies, source boundaries and voice
placeholders. Concurrent requests do work; these results still require review and
do not establish reliable unattended implementation.

User selected USB phone testing. User-local build tools are installed under
`~/.local/share/daleelak-dev`: Temurin JDK 17, Gradle 9.3.1 and Android command-line
tools, each archive verified against its publisher checksum. Android 36.1 SDK
and build tools installation/assembly are underway; no APK/device run yet.

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

## Latest step-card update

The user replaces the flowchart visual with cards. UI now uses Compose VerticalPager, a current overview covering all ready unfinished tasks, one card per step and a final operation-actions card. Upcoming cards remain viewable and retain prerequisite completion checks. Checklists are grouped by category and known formats are shown. Long content scrolls within a card; previous/next controls provide an alternative to swiping. No domain graph or AI schema change is required for this visual update. The existing source/place catalogs are still pending, so the UI must not fabricate source detail or locations.

**Possible improvement only:** location-based whole-trip planning for the current parallel-task stage. The other agent should preserve dependency/source/place identifiers that could support this later, without adding a live routing API or invented itinerary now.

Superdesign sign-in to Personal succeeded. The canvas is https://superdesign.dev/teams/ade11b66-72b7-4b09-995e-39eb882d8599/projects/63aa42fd-4f82-49d4-b314-99fafe99596b. Selected source context is documented under `.superdesign/context-files.md`; this is a browser review artifact, while the product remains native Android. No native compilation or runtime inspection is established; Java and Gradle are not available in this shell. No tests were added or run.
