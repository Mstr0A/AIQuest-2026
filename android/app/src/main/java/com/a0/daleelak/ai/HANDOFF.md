# Current service coverage

Read docs/daleelak/three-service-demo.md first. AdditionalReviewedAssistant and guidance/additional-services.json add three distinct historical service cards. OpenRouterAssistant extracts five fields, and ResponseValidator accepts only exact reviewed per-service plans. UI key entry is removed; debug builds embed a locally supplied key from the ignored secrets file. Earlier notes below are historical; preserve legacy lost-book records.

# Assistant/state handoff — 2026-10-09

The assigned state-layer work is implemented here. UI files were not edited.

## Agreed observable interface

`currentPlan: GuidancePlan?`, `suggestedPrompts: List<String>` (exactly three), `isResponding: Boolean`.
Existing screen-facing properties/actions remain. `showPlan` derives from `currentPlan != null`.
`savePlan()` snapshots the accepted current plan; it never reads `DemoCatalog.plan`.
Setting `selectedId` restores operation context. Returning to Assistant retains that context.
The UI must replace its existing direct `DemoCatalog.plan/prompts` reads with these state properties.
The old fixture remains for source compatibility only until the UI owner removes those reads.

## Grounding boundary

Assets contain the unchanged v1.0 JSON schema and a provenance-labeled source record derived ONLY from
`docs/daleelak/sources.md`. This record is a supplied recorded-inspection summary, not a fetched/complete
service-card excerpt. It establishes an electronic route and police-report prerequisite. Full branch rules,
fees, document format, follow-up sequence, places and availability remain explicit gaps.

The local deterministic matcher is not a connected AI. It recognizes a limited set of Arabic/English issue
phrases and prerequisite answers, reuses answers, and permits corrections. An ambiguous message asks for
the supported issue; missing procedure rules do not trigger profiling. Unknown/unsupported coverage is
different from transport/validation failure. Provider choice, authentication and audio transcription remain unresolved.

Typed response DTOs round-trip through the original schema. The validator checks IDs, source references,
checklist/step uniqueness, conditional requirements and acyclic dependencies. Procedural content must match
the narrow reviewed templates; references alone are not accepted as proof. App-rendered procedural text
comes from those templates, not arbitrary provider prose. Future reviewed coverage needs explicit records
and corresponding validation, rather than weakening checks to allow model-invented facts.

## Persistence and replanning

Reads storage versions 1 and 2 from the original preference namespace; writes version 2, preserving plan
snapshots, metadata, messages, answers and validated response context. Active session and editable text are
also stored locally. No audio, credentials or government records are stored. Storage read failure disables
writes instead of overwriting unreadable records. Writes serialize and run on IO dispatchers.

Task equality/requirements/dependencies determine whether progress remains valid. Unchanged work stays
complete, affected dependent steps reopen, and demo events survive. Replanning never confirms official progress.
Completion means the user confirmed the saved portion, not official approval or full procedural completion.
Booking remains a labeled local event and does not change progress.

## Additional accessors — PROPOSAL, not integrated

Coordinate with the UI owner before consumption: `sourceFor(id)` returning `ReviewedSource?`,
`questions`/case summary and explicit source gaps, `placesForStep(id, origin)` with app-computed distance,
and a transcription-result-to-editable-draft action. None is exposed as an agreed UI API yet.
The catalog has no verified place entries. TranscriptionGateway remains separate from the chat wire contract.
No provider credentials or new external service is assumed.

No implementation tests were added or run. Build/type checking and tool availability are reported separately.

Verification: `:app:compileDebugKotlin --offline` passed with the existing UI sources. Full debug assembly
reached compilation/resources but was blocked by sandbox access to `debug.keystore.lock` (and native strip
permissions); no successful APK assembly or device run is claimed. `detekt` and `ktlint` are not installed
in this session, so their checks could not be run. `git diff --check` and schema-copy hash comparison passed.
Repeated remote pull attempts remain blocked by the session's GitHub connection failure; local docs were
read from commit `4fd9e09`. This handoff documents the assigned work without editing the UI owner's files
or changing canonical documents/schema.
