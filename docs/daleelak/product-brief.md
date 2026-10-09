# Product brief

## Idea

DALEELAK helps people describe what they need in ordinary language, understand the relevant procedure and organize its completion. The assistant asks questions that affect the route, presents a dependency flowchart, explains each step's requirements and supplies separate checklists. Users save their operations locally and return to the next action.

**Assistant focus:** understand the problem and select its documented solution path. Back-and-forth establishes issue details, not a general profile of the person. The supplied documents are the only authority for government procedures. Ask a personal category only when a reviewed rule makes it necessary to choose a route or requirement; do not collect a biography, identifiers or unrelated preferences. If the documents do not establish a rule, state the gap rather than guessing or extending the interview.

The intended outcome is less uncertainty about what to do, what to bring and where to go. Reduced visits, completion time, adoption and nationwide coverage are intended benefits, not measured results.

## Intended users and pilot

People navigating Jordanian government services, including users unfamiliar with formal service terminology. The first demo covers a Jordanian citizen seeking replacement of a lost family book. Support for other services requires reviewed source records; the supplied PDF covers CSPD rather than all government processes.

## Confirmed demo requirements

| Feature | Concrete behavior |
| --- | --- |
| Conversation | Interpret the user's problem and ask relevant clarifying questions until a usable route is understood. |
| Plan | Display an ordered dependency flowchart; a list can provide the same content accessibly. |
| Step details | Explain prerequisites and additional requirements, including physical/digital format where known. |
| Checklists | Separate documents, actions, payments/commitments and relevant visits within the steps. |
| Places per step | Show up to three nearest relevant options with clickable map links. |
| Locations section | Government service centres and relevant shops, sorted by distance and available information about availability. |
| Persistence | Save operations and progress in local storage; group history as new, ongoing or completed. |
| Voice | Dictation for everyday Jordanian Arabic, English and mixed speech; transcript is editable before sending. Dialect accuracy remains unverified. |
| Demo integrations | Keep booking and other Sanad-dependent controls as interactive placeholders that produce clearly labeled local fake responses. |
| Suggested prompts | Show three relevant prompts; claim popularity only if supported by actual data. |
| AI output | Use a documented structured response shape rather than relying on arbitrary prose. |

Photo/studio requirements are generic examples for applicable transactions. The inspected lost-family-book electronic card does not list a portrait requirement; do not add one to make the places feature look richer.

## Architecture boundaries

- Local storage only for the demo. No accounts, cloud history or cross-device synchronization.
- The AI API returns the information used to construct the explanation/plan. Do not silently add live government, maps, geocoding, availability or other service APIs.
- Public map/official-channel links can open external services when clicked. Opening a link is distinct from an integrated transaction API.
- Bundle reviewed source records and a small verified location catalog locally where available. An AI-only runtime still needs that grounding input.
- Browser location permission or manual location selection supplies the distance origin. The model should not guess the user's location.
- Voice transcription must fit the same API boundary or be demonstrably local. Browser speech recognition may use an external service; a transcription implementation has not been selected.
- Any provider secret needs a server-side proxy or another suitable credential arrangement. A proxy to the AI provider does not imply accounts or server-side operation storage. The hosting/proxy choice is unresolved; never place a private key in client code or local storage.

## Explicit future development

1. Integration inside Sanad and appointment booking.
2. Choice between Sanad and in-person routes, with brief Sanad tutorials.
3. Personal expiry/required-document notifications that open a contextual procedure conversation.

Latest user clarification retains **simulated booking and other Sanad-dependent placeholders in the demo**. Their real integrations remain future. No live bookings, personal government records, payments, submissions or status feeds have been established. See [voice and demo integrations](voice-and-demo-integrations.md).

## AI contribution

Interpret everyday Arabic, select relevant questions, map answers to reviewed conditions and explain an adapted plan. Graph rendering, distance calculation, history and state transitions should remain deterministic app behavior. The model must not decide that a government approval occurred.

## Open choices

UI layout, palette, AI provider/model, transcription method, framework, location catalog, verified hours/service coverage and final reviewed branch records. The earlier Teal and Sand palette was an assistant recommendation, not a user selection.
