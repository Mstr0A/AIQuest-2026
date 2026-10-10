# Android project handoff

Read README.md and ../docs/daleelak before substantive changes.

User directive on 2026-10-09: native Android only, no KMP or web scaffold.
The user authorized creation of this Android skeleton in the IntelliJ-generated project.
The user subsequently authorized copying this project into the repository and pushing the current branch. This supersedes the earlier documentation-only authorization statement.
Keep Jetpack Compose, Kotlin, package com.a0.daleelak and the generated build baseline unless a change is necessary.
Do not add or run implementation tests without a user request; compiling/debug assembly is allowed.
Preserve local-only operations, no accounts, exactly three prompts, voice adapter boundary,
lost-family-book scope and labeled interactive Sanad/booking simulations.
No invented procedural facts, places, requirements, fees, hours, booking slots or official outcomes.
Accepted plans now come from the reviewed local catalog and validated structured
assistant state; the runtime assistant is deterministic, not a connected AI.
Chat consumes currentPlan/suggestedPrompts/isResponding instead of DemoCatalog fixtures.
Preserve the unchanged wire schema and source/graph/ID validation before connecting AI output.
Another agent is working on the linked repository. Check local changes and attempt pulls frequently.
Do not overwrite concurrent work or assume remote synchronization when network access fails.
Canonical docs live in ../docs/daleelak. Read those current files rather than the original IntelliJ project snapshot.

Latest UI direction: home opens new-operation chat and all/current/finished operation lists.
Current includes new/ongoing local operations; finished means user-confirmed completion.
Chat defaults to voice controls, keeps text readable, and reveals keyboard typing on demand.
Audio input is explicitly deferred; current microphone/listening controls are honest placeholders.
Spoken replies should be the default after playback is implemented, retaining text and stop/replay controls.
Use the fixed minimalist Navy and Sky theme. UI files remain separate from the other agent's ViewModel/domain/data ownership.

U-041–U-043 authorize separate worker tasks, build/run and immediate source push.
Two separate chat/journey worker calls were reviewed and completed locally; source
batch pushed as 3c72157. User selected USB phone deployment. Local assembly now
succeeded after fixing DaleelakIcons pathBuilder parameter; APK signing verified.
This shell is containerized without /dev/bus/usb or /dev/kvm, so device install/launch
must run from the normal desktop terminal with tools/android/run-phone.sh.
U-044: the user confirms the app opens after the normal-terminal installer. Record
this as user-confirmed launch, not an agent-observed full walkthrough. Voice and
places remain pending. No general implementation tests were added.

U-049, 2026-10-10: Waydroid is now available directly to this shell through adb at
192.168.240.112:5555. This supersedes the earlier requirement to install from an
outer terminal when testing Waydroid. Use ../tools/android/run-phone.sh with the
explicit serial; inspect screenshots, UI XML and logcat. Read the current speech
research report in ../docs/daleelak before further voice implementation. The goal
remains paused; research recommendations are not user-selected providers. This
API 33 Waydroid image has no SpeechRecognizer service; cloud capture must check
microphone routing separately. OpenRouter remains app-runtime-only.

U-050, 2026-10-10 (latest): Suitable speech models are verified on OpenRouter, so
the user explicitly authorizes implementation and live app-flow tests now,
superseding the coding pause above. Use session-only OpenRouter credentials for
elevenlabs/scribe-v2, elevenlabs/eleven-v4-turbo and the existing issue classifier.
Transcripts remain editable and unsent; only accepted procedural text is spoken.
Stop capture/playback on navigation, backgrounding and new conversations. Never
use OpenRouter for coding/planning workers. See the implementation handoff in
../docs/daleelak/openrouter-voice-implementation.md; accent quality is not proven
by synthetic integration fixtures. Build with ../tools/android/build-local.sh to
reuse the original debug key and preserve installed history.

- U-068: The user explicitly requires continuing until an ordinary Jordanian lost-passport inquiry reaches document-grounded guidance, then verifying the other covered services carefully. The user selected verification of the existing birth-certificate, CSPD-document-certification, declared-address and family-book cases first, rather than loading the entire 2024 guide. Live runtime tests are authorized. Use reviewed PDF pages 48–49, ask passport type when unknown, preserve conditional requirements and source gaps, and keep the replacement credential private. Suggestions remain disabled. See lost-passport-demo.md for scope and evidence.

- U-070: User explicitly approves replacing the restrictive classifier pipeline with document-grounded conversational reasoning and structured steps. Runtime should use reviewed source content and conversation history, handle uncertainty without canned rejection, answer follow-ups naturally, and preserve source/step validation. Approval supersedes the old six-field classifier architecture. No coding workers or key publication are authorized.

- U-071: The user says time is short, asks for a quick environment check, rejects hardcoded prerequisite logic, and explicitly authorizes proceeding with document-driven AI. Remove live prerequisite/fact/question whitelists and fixed canonical-plan equality. AI derives requirements and produces structured steps from supplied reviewed source text and conversation history; code validates response structure, citations and dependency integrity. Do not equate a valid citation with proof of factual correctness. Keep the replacement key private and OpenRouter for app runtime/testing only. Latest architecture is documented in grounded-conversation.md.
