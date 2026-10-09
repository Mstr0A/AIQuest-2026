# DALEELAK

**An AI guide that turns a citizen's government-service goal into a clear, personalized plan with requirements, relevant places and saved progress.**

DALEELAK is the team's Society-sector AI Quest 2026 concept. The current flagship journey is replacing a lost family book (دفتر العائلة). The hackathon app demonstrates a potential future Sanad extension. It does not perform government transactions or establish access to protected Sanad APIs.

## Read in this order

1. [Product brief](product-brief.md): idea, intended outcome and confirmed scope.
2. [App flow](app-flow.md): chat, steps, requirements, places and saved operations.
3. [AI response contract](ai-response-contract.md): proposed request/response structure and handling rules.
4. [JSON Schema](ai-response.schema.json): proposed response shape.
5. [Grounding sources](sources.md): usable references, provenance and known gaps.
6. [Agent instructions](AGENTS.md): constraints future contributors must preserve.
7. [Voice and demo integrations](voice-and-demo-integrations.md): dictation pipeline and simulated Sanad actions.
8. [Visual direction and palette options](visual-direction.md): required minimalist UI and the selected Navy and Sky palette.
9. [Installed UI skills](skills.md): project Android/Material You and Kotlin guidance, sources and limits.

## Current versus future

**Current demo:** local storage, no accounts, conversation/clarification, flowchart, per-step checklists, up to three relevant nearby map options, a locations view, voice-to-text targeting Jordanian Arabic/English/mixed speech, three suggested prompts and new/ongoing/completed history. Sanad-dependent features including booking remain interactive placeholders returning labeled local fake responses. Information is returned through the AI API. Location/source reference data can be bundled locally.

**Future real capabilities:** integration inside Sanad, actual booking, Sanad/in-person route comparison and tutorials, and personal-document reminder data. Where shown in the current demo, integration-dependent actions are placeholders, not verified capabilities.

**Minimalist UI is required. Navy and Sky is the user-selected palette.** Exact layout is undecided; the user will supply their UI vision. This folder contains product documentation and a proposed contract, not an implemented app. JSON shape constraints do not by themselves establish factual correctness.

Scope owner: user/team. Latest directive received 2026-10-09. Approximately 11 practical build hours was the earlier user-reported constraint; remaining time has not been remeasured.
