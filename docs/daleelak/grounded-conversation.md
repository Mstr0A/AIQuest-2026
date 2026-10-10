# Document-driven AI conversation

## Latest approved architecture

U-071 supersedes the earlier compact classifier and fixed-plan resolver. The user explicitly rejects hardcoded prerequisite lists and asks the AI to find relevant requirements in the supplied documents.

The live assistant reads reviewed document extracts and workflow transcriptions, the last 24 chat messages and accepted user facts. It uses `z-ai/glm-5.3-flash` with high reasoning and a 16384-token output allowance for structured plans. The model derives relevant facts, asks natural questions, selects the phase, and generates structured step cards. Facts, question keys, service IDs, step counts and step wording are not limited to a coded government-service script.

The document context contains source text and documented limits, not `required_fact`, `supported_value`, approved question lists or enum routing rules. Existing curated cards supply reviewed workflow transcriptions as source material; they do not gate the live response. The AI can group related authority actions, handle parallel dependencies, apply documented conditions and produce conditional/partial guidance when source or user details are incomplete. Unsupported guidance is written by the model, not `UnsupportedGuidance`.

## What code validates

The existing JSON schema, valid citation IDs, citation inclusion, unique fact/step/checklist IDs, required conditions, acyclic dependencies and lack of unverified place IDs. The code does not require a specific prerequisite answer, question ID, service ID, template match or step count. Source limits are appended automatically so historical rules and incomplete coverage remain visible. Suggested-response UI remains hidden.

Citation validation proves that a referenced document exists, not that every generated claim is true. Source-only prompting and inspection of live outputs remain necessary. Model-written procedures can contain errors; this is not verified government execution. Sources are reviewed extracts for four complete workflows and the partial lost-family-book procedure, not the full 162-page guide. Loading further raw document sections remains future work.

## Verification

`ThreeServiceLiveTest` now tests document selection and usable generated plans rather than predetermined scripts. Scenarios cover three distinct services, a natural lost-passport conversation, a fee/guarantee follow-up, the partial family-book path and unsupported education certification. Local tests explicitly accept arbitrary source-relevant question/fact keys and reject unknown citations and dependency cycles. Old tests requiring exact canonical wording or rejecting every changed procedural phrase were replaced because they tested the superseded architecture.

Results are stored in `evidence/document-ai/`. Tests use the actual gateway, codec and validator in the JVM; they are not an Android UI walkthrough. Current credentials remain private local files and the local debug APK only.

Final verification: all 5 tests passed without skips, including 8 live app-gateway requests across five covered services and an unsupported education request. The APK was rebuilt and copied to Downloads/DALEELAK-demo.apk. Source-only correctness is not established merely by the structural test pass.
