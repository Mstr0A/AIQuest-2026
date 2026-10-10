# Document-driven AI conversation

## Latest approved architecture

U-071 supersedes the earlier compact classifier and fixed-plan resolver. The user explicitly rejects hardcoded prerequisite lists and asks the AI to find relevant requirements in the supplied documents.

The live assistant reads reviewed document extracts and workflow transcriptions, the last 24 chat messages and accepted user facts. It uses `z-ai/glm-5.3-flash` with high reasoning and a 16384-token output allowance for structured plans. The model derives relevant facts, asks natural questions, selects the phase, and generates structured step cards. Facts, question keys, service IDs, step counts and step wording are not limited to a coded government-service script.

The document context contains source text and documented limits, not `required_fact`, `supported_value`, approved question lists or enum routing rules. Existing curated cards supply reviewed workflow transcriptions as source material; they do not gate the live response. The AI can group related authority actions, handle parallel dependencies, apply documented conditions and produce conditional/partial guidance when source or user details are incomplete. Unsupported guidance is written by the model, not `UnsupportedGuidance`.

## What code validates

The existing JSON schema, valid citation IDs, citation inclusion, unique fact/step/checklist IDs, required conditions, acyclic dependencies and lack of unverified place IDs. The code does not require a specific prerequisite answer, question ID, service ID, template match or step count. Source limits are appended automatically so historical rules and incomplete coverage remain visible. Suggested-response UI remains hidden.

Citation validation proves that a referenced document exists, not that every generated claim is true. Source-only prompting and inspection of live outputs remain necessary. Model-written procedures can contain errors; this is not verified government execution. Sources are reviewed extracts for five complete workflows and the partial lost-family-book procedure, not the full 162-page guide. Loading further raw document sections remains future work.

## Verification

`ThreeServiceLiveTest` now tests document selection and usable generated plans rather than predetermined scripts. Scenarios cover three distinct services, a natural lost-passport conversation, a fee/guarantee follow-up, the partial family-book path and unsupported education certification. Local tests explicitly accept arbitrary source-relevant question/fact keys and reject unknown citations and dependency cycles. Old tests requiring exact canonical wording or rejecting every changed procedural phrase were replaced because they tested the superseded architecture.

Results are stored in `evidence/document-ai/`. Tests use the actual gateway, codec and validator in the JVM; they are not an Android UI walkthrough. Current credentials remain private local files and the local debug APK only.

Final verification: all 5 tests passed without skips, including 8 live app-gateway requests across five covered services and an unsupported education request. The APK was rebuilt and copied to Downloads/DALEELAK-demo.apk. Source-only correctness is not established merely by the structural test pass.

## Lost national ID and UI update

Pages 38–39 were present in the original guide but were not in the runtime reviewed extracts. The new `cspd-2024-lost_national_id` source supplies visually reviewed requirements, historical fees and the complete illustrated workflow directly to the AI. It has no app-authored prerequisite gate or fixed plan. The source distinguishes the first/second/third loss, conditional military/bridge-card documents, guarantees and undertakings, and the two-week restriction after police notification (unless director approval). The guide does not explain the electronic police notification mechanism or specify photo dimensions.

The Arabic title is larger and bold. Places navigation and per-card Places placeholders are removed. Any step can be confirmed, which completes that step and every preceding displayed step, plus necessary graph dependencies. Reopening a step clears dependent completions. Final operation completion remains a separate user confirmation. Step cards now have a visible content scrollbar and a clickable page navigation strip.

Lost-ID verification passed: two real app-gateway turns (`هويتي ضاعت، شو أعمل؟`, then an explicit first-loss request) produced a relevant clarification followed by a cited eight-card plan. The response includes the historical 5 JOD fee and two-week restriction. See `evidence/lost-national-id/`. Final UI build succeeded; the UI changes were compiled, not exercised in an automated interaction test.

## Reversible archive

Operation deletion is replaced by archiving. A persisted `archived` boolean defaults to false for existing local records. Archived operations retain status, conversation, plan and progress; they are excluded from active lists and counts and shown under the Home archive entry replacing history. Archived plans are viewable with progress controls disabled, and can be restored. Live conversation saves do not reuse archived records.

## Branding and automatic voice submission

Home uses a 64 sp bold Arabic title. The supplied SVG is preserved in `assets/branding/daleelak.svg` and converted to native vectors for every screen header and the launcher/adaptive icon. Final speech transcripts now submit automatically through the same validated assistant gateway. Empty results and cancelled/stale voice sessions are not sent; typed drafts remain available after failures. This supersedes mandatory transcript review.

## Language and text size

The assistant follows the dominant language of the latest substantive inquiry. English inquiries receive English guidance; Arabic speech with a few English terms stays Arabic. Brief replies inherit the conversation language and explicit language requests take precedence. Reviewed source-limit translations are bundled so automatic warnings match English replies.

The common header provides an Arabic/English switch and a Default/Large/Larger text-size cycle (1.0, 1.2, 1.4 times the system font scale). Both are stored locally. Interface labels use Arabic/English resources and switch RTL/LTR direction. The interface setting does not dictate AI response language or translate saved user conversations, model-generated plans, or original source titles.
