# DALEELAK

> **Current scope, 2026-10-10:** The live AI reads reviewed 2024 document extracts for birth certificates, CSPD-document certification, declared-address updates, lost ordinary passports and lost national ID cards, plus partial lost-family-book guidance. It derives questions and step plans from the documents and conversation; no fixed prerequisite classifier gates the live response. See [current architecture](grounded-conversation.md). Places UI is removed; step confirmation completes earlier displayed steps. The earlier prototype descriptions below are historical.


**An AI guide that turns a citizen's government-service goal into a clear, personalized plan with requirements, relevant places and saved progress.**

DALEELAK is the team's Society-sector AI Quest 2026 concept. The current flagship journey is replacing a lost family book (دفتر العائلة). The hackathon app demonstrates a potential future Sanad extension. It does not perform government transactions or establish access to protected Sanad APIs.

## Read in this order

1. [Product brief](product-brief.md): idea, intended outcome and confirmed scope.
2. [App flow](app-flow.md): chat, steps, requirements, places and saved operations.
3. [AI response contract](ai-response-contract.md): implemented envelope and separately tested full-response prototype.
4. [JSON Schema](ai-response.schema.json): response shape enforced by the app.
5. [Grounding sources](sources.md): usable references, provenance and known gaps.
6. [Agent instructions](AGENTS.md): constraints future contributors must preserve.
7. [Voice and demo integrations](voice-and-demo-integrations.md): dictation pipeline and simulated Sanad actions.
8. [Visual direction and palette options](visual-direction.md): required minimalist UI and the selected Navy and Sky palette.
9. [Installed UI skills](skills.md): project Android/Material You and Kotlin guidance, sources and limits.
10. [Skeleton review and work split](work-plan.md): inspected Android code, remaining gaps, file ownership and the other agent's task.
11. [Coding workflow and costs](coding-workflow.md): Codex orchestration with a scoped OpenRouter coding worker; [runner setup](../../tools/coding-worker/README.md) and [first-run result](worker-first-run.md), including actual cost and review corrections.

Latest handoff, 2026-10-10: [Jordanian speech research and Waydroid verification](speech-research-2026-10-10.md). The implementation goal is paused. OpenRouter is restricted to app runtime; historical coding-worker material is superseded for new work. Speechmatics and ElevenLabs are recommendations, not selected or measured providers.

## Current versus future

**Current demo:** local storage, no accounts, conversation/clarification, snapping step cards and a current-task overview, per-step checklists, up to three relevant nearby map options, a locations view, voice-to-text targeting Jordanian Arabic/English/mixed speech, three suggested prompts and new/ongoing/completed history. Sanad-dependent features including booking remain interactive placeholders returning labeled local fake responses. Information is returned through the AI API. Location/source reference data can be bundled locally.

**Future real capabilities:** integration inside Sanad, actual booking, Sanad/in-person route comparison and tutorials, and personal-document reminder data. Where shown in the current demo, integration-dependent actions are placeholders, not verified capabilities.

**Minimalist UI is required. Navy and Sky is the user-selected palette.** UI is being shaped incrementally by the user: home navigation, voice-first chat, and snapping step cards are now specified. A native Kotlin/Compose skeleton now exists in `../../android/`; it uses illustrative guidance and scripted chat, with home navigation, voice-first controls and the selected Navy and Sky theme. These docs describe intended behavior, not proof that every feature is implemented. JSON shape constraints do not by themselves establish factual correctness.

Scope owner: user/team. Latest directive received 2026-10-09. Approximately 11 practical build hours was the earlier user-reported constraint; remaining time has not been remeasured.

## Full-response context prototype

See [context preparation](assistant-context-guide.md), [system prompt](assistant-system-prompt.txt), [approved context](assistant-context.json) and [scenario fixtures](assistant-scenarios.json). The deployed model still extracts issue facts; this prototype does not change app behavior.

Direct prototype result: [7/7 scenario checks and limitations](assistant-evaluation.md).
