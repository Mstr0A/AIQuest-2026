# App flow

This is a product specification, not an official procedure. Government requirements come from reviewed sources; scenario events below are illustrative.

## 1. Start or resume

The user starts a new conversation or opens an existing operation. Show three suggested prompts. Use relevant context when available; generic starters are acceptable without falsely labeling them popular.

Initial examples: “ضاع دفتر العيلة، شو أعمل؟”, “شو الأوراق المطلوبة لتعويض دفتر العائلة؟”, “عندي بلاغ فقدان، شو الخطوة الجاية؟”. These are examples for the pilot, not measured popular questions.

Voice-to-text fills an editable message. Let the user correct the transcript before sending. Denied microphone permission, unsupported transcription or transcription failure leaves typed input available.

## 2. Clarify and confirm

Ask questions that change the procedure or next action. Reuse answers already provided. For the flagship, relevant distinctions include lost/damaged, inside Jordan/abroad, first/repeat loss, applicant relationship, relevant identity/account availability and prerequisites already completed. Do not demand unnecessary identifying numbers merely to explain a procedure.

Show a concise case summary with a correction action. Missing facts remain explicit. A user who has already completed a prerequisite can enter halfway through the process.

## 3. Present a plan

The response provides stable step identifiers, dependencies and source-linked requirements. The app renders a flowchart and can offer the same content as a numbered list. Highlight the next actionable step and distinguish tasks from waiting for an external response.

Illustrative flagship outline: clarify/prepare, complete required prerequisite, submit through the official channel, await a response, complete the required follow-up, receive readiness information, collect and finish. Additional official instructions may create an action-required state. Exact procedures, fees and formats must stay grounded; do not invent an authority's internal workflow.

Changing a relevant answer revises affected steps. Preserve completed work unless the revision genuinely invalidates it; show the change rather than silently resetting the operation.

## 4. Expand a step

Each step shows its purpose, actor, preceding dependencies, extra requirements, applicable checklists, expected completion evidence and source references. Requirement format can be original, paper copy, digital upload, official electronic record or unspecified. Unspecified is preferable to an invented format.

Checklist categories: documents, actions, payments/commitments and visits. Each requirement is required, conditional or helpful. Conditional requirements name their condition. Keep payable fees separate from guarantees/undertakings; do not infer a total cash payment from a commitment's face value.

Step chat can answer “Do I need to print this?” using the opened step's context. “I lost my identity card too” should trigger clarification/replanning where the reviewed rules support it.

## 5. Show relevant places

Inside each step, show **up to three** nearest relevant options with clickable maps links. The separate location section exposes the larger relevant catalog. Include shops only when a sourced requirement makes that type useful.

Relevance comes first: a nearby centre may not offer the required service. The app joins model-selected place IDs to a trusted local catalog and computes distance from a user-selected origin. Label straight-line distance as such; driving distance requires a separate data source not currently authorized in the demo architecture.

Availability distinctions: known opening hours, estimated open/closed from dated hours, actual booking slots, service coverage and unknown status. They are not interchangeable. There is no established live availability feed. If only static hours exist, display their date and label the estimate; keep unknown entries visible rather than guessing. Provide distance sorting and an availability-first option with distance as a secondary criterion, using only established information.

If fewer than three suitable entries exist, show the available entries and a clear limitation. If the origin is unknown, ask for an area or location permission; do not invent distances. Missing verified catalog data is a dependency, not evidence that no service exists.

## 6. Save progress and history

Operation-level history states are **new**, **ongoing**, **completed**. Proposed local transitions: new when a draft is saved before an action starts; ongoing once action/submission/waiting begins; completed after the user confirms the outcome. Allow correction/reopening.

Step states can include to do, ready, user-reported complete, submitted, waiting and action required. Store them separately from AI-generated instructions. Without a government feed, a status is user-reported; an AI response cannot confirm approval or completion.

Persist operation ID, goal/title, answers, conversation context needed to resume, accepted plan/source version, user-entered progress and any references the user chooses to save. The app generates IDs/timestamps. Storage limitations or write errors should leave current work visible and explain that saving failed. Provide local deletion; clearing browser storage removes history.

## 7. Future routes, booking and reminders

Future Sanad integration adds route choice, Sanad tutorials, booking and consented personal-document reminders. A reminder opens a draft contextual conversation and confirms circumstances before planning. Public open data does not contain a user's document-expiry dates.

## Demo sequence

1. Describe a first local loss in Arabic.
2. Answer a relevant question and accept the summary.
3. Open a step, its checklists and relevant map options.
4. Save, leave and resume at the next action.
5. Change one relevant answer and show the revised requirements.
6. Show user-reported progress and complete/reopen the operation.

No booking is needed in the current demo. UI layout remains undecided.
