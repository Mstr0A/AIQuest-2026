# AI response contract — v1.0

> **Current scope:** Three separate reviewed 2024 services are now implemented: registered-birth certificate, CSPD-document certification and declared-address update. See [three-service demo](three-service-demo.md). Earlier lost-book-only prototype scenarios below are historical; the deployed classifier now accepts five bounded fact fields.


## What is implemented

The Android app already uses this response envelope, JSON shape validation (`ContractCodec`), reviewed-content validation (`ResponseValidator`) and a presenter that rebuilds procedural prose from accepted facts and the reviewed catalog. The live model currently extracts issue facts; local reviewed templates construct the full response. The full-envelope model prompt below is a separately tested prototype, not deployed Android behavior.

- Schema: `android/app/src/main/assets/guidance/ai-response.schema.json`; the documentation copy is `ai-response.schema.json`.
- Full-response system prompt: [assistant-system-prompt.txt](assistant-system-prompt.txt).
- Approved questions, goals and partial plan: [assistant-context.json](assistant-context.json).
- Scenario fixtures: [assistant-scenarios.json](assistant-scenarios.json).
- Context preparation: [assistant-context-guide.md](assistant-context-guide.md).

## Request context

Supply the latest message, known issue answers, pending question ID, reviewed source records, approved questions/plan and eligible locations. Reuse answers instead of interviewing the user again. Do not send identity numbers or build a personal profile. The app owns operation IDs, timestamps, progress, coordinates, map links and distance calculations.

The current pilot knows only `issue` (lost/damaged/other) and `police_report` (yes/no/explicitly unknown). Lost/damaged refer specifically to a family book. Loss location and prior loss history are not approved questions: the current records do not establish the corresponding branch rules. A missing government rule is a source gap, not a reason to ask more personal questions.

## Response envelope

All fields are required; unknown object keys are rejected.

| Field | Meaning |
| --- | --- |
| `schema_version` | Exactly `1.0` |
| `kind` | `clarification`, `plan`, or `unsupported` |
| `message` | Short explanation; raw model prose is not trusted procedural evidence |
| `case_summary` | Goal label, accepted issue facts, unresolved issue facts |
| `questions` | Approved clarification objects; empty for plans/unsupported |
| `plan` | Null for clarification/unsupported; reviewed partial plan otherwise |
| `suggested_prompts` | Exactly three approved strings |
| `source_ids` | References resolving to supplied source records |
| `uncertainties` | Explicit source gaps and approved unresolved conditions |

Plan steps include stable IDs, dependencies, actor, completion evidence, source IDs and up to three eligible place IDs. Checklists separate `documents`, `actions`, `payments_and_commitments` and `visits`. Items include necessity, condition and format. Use `unspecified` when the source establishes no physical/digital format. A reference ID alone does not establish that a claim is supported.

## Current pilot decisions

1. Issue unknown: ask the approved issue question.
2. Lost family book, report answer absent: ask the approved police-report question.
3. Lost family book, report yes/no/explicitly unknown: return the same approved partial plan, all known source gaps and no extra questions. Explain the appropriate immediate action. A user saying they have a report is not official verification or automatic completion.
4. Damaged family book or another service: unsupported. Do not reuse the lost-book procedure.

The plan currently supports only a police-report prerequisite and review of the electronic service channel. No verified fee, portrait requirement, submission format, processing time, complete branch procedure or location catalog is available. No report filing method is invented. Missing locations produce empty `place_ids`.

## Acceptance and failure handling

Validate JSON shape, known IDs, unique facts/steps/items, checklist conditions and acyclic dependencies. The pilot also requires exact approved questions and plan content. Reject fabricated requirements even if accompanied by an existing source ID. The prototype evaluation additionally checks expected facts, canonical goal labels and unresolved facts; these stricter checks are not all implemented in the Android validator.

Native `response_format: json_schema` with `strict: true` and `provider.require_parameters: true` is used in the direct prototype. Provider enforcement does not replace app validation or source review. Free procedural prose is rebuilt by the Android presenter; successful schema validation alone is not proof of factual grounding.

Transport/model errors are retryable app errors, not fabricated unsupported answers. Preserve the unsent draft and previous valid operation. Do not display partially parsed instructions or silently alter progress. Any future repair attempt must be bounded and pass the same acceptance checks; the prototype runner does not automatically retry.

## Minimal supported clarification

For “ضاع دفتر العيلة، شو لازم أعمل؟”, the envelope has `kind: clarification`, `plan: null`, known fact `issue=lost`, unresolved fact `توفر بلاغ الشرطة` and this exact question:

```json
{
  "id": "police_report",
  "text": "هل عندك بلاغ فقدان من الشرطة؟",
  "options": ["عندي بلاغ فقدان", "لسه ما عندي بلاغ", "مش متأكد"],
  "reason": "المصدر المراجع يذكر البلاغ كمتطلب سابق؛ الإجابة تحدد إذا نبدأ بالبلاغ أو بالقناة الإلكترونية."
}
```

The full prompt uses Arabic approved UI content while understanding Arabic, English and mixed input. It does not ask where the loss occurred or whether this is a repeat loss without reviewed branch rules.
