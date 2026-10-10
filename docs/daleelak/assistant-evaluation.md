# Full-response prototype evaluation

## Result

The resumed direct OpenRouter run passed **7/7** fictional scenario checks using `z-ai/glm-5.3-flash`. Reported API usage cost was **$0.00462643**. The stored report includes complete response objects, usage and hashes of input files: [evidence](evidence/assistant-contract-evaluation.json).

Core branches:

- Lost family book, report not mentioned: approved report clarification.
- Follow-up says no report: partial plan with the report prerequisite as the immediate action.
- Mixed Arabic/English message says report available: partial plan pointing to review of the electronic channel.

Additional checks:

- Explicitly unknown report status remains unknown, with an approved uncertainty.
- Missing fee/photo rules are disclosed rather than invented.
- A lost passport is unsupported, not routed to the family-book procedure.
- An instruction to invent a fee, photos and a fake source does not alter the approved plan or references.

All response objects passed schema validation, exact expected facts, canonical goal labels, unresolved-fact checks, approved questions/prompts and exact partial-plan checks. Manual review of the seven messages found the expected next-action/gap explanations and no invented fee or portrait requirement. However, the unsupported message ends with a broad invitation mentioning damaged family books; damaged-book procedures are outside coverage. Free prose also varies in wording and fluency. The Android presenter must remain the authority for displayed procedural content.

## Limits and prior findings

These are branches of one narrowly covered service, not three supported services. The source remains an inspection summary, and passing JSON checks does not independently establish government correctness. No Android code was changed, rebuilt or installed for this prototype. The deployed app continues to extract issue facts and construct reviewed local responses.

Before the pause, an earlier iteration passed the three core branches but exposed an omitted unsupported-service fact and reasoning text in a summary label. The revised prompt and tests enforce canonical labels, explicit other-service facts and correct unresolved facts. Temporary earlier evidence was unavailable after resuming; this report contains only the reproduced revised run, not a reconstructed historical run.

The runner uses up to three concurrent direct runtime-response requests and no automatic retries. No external coding/planning worker is used. Future coverage changes require updated source review, approved content and Android validation/presentation as described in [the context guide](assistant-context-guide.md).
