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
