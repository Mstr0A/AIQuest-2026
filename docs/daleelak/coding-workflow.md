# Cost-conscious coding workflow

User requests, 2026-10-09 (U-038/U-039): be more careful with usage, consider this Codex agent as orchestrator and an open model through the user's OpenRouter key for implementation, then explain how to start. A lightweight local runner and first task packet are prepared in [tools/coding-worker](../../tools/coding-worker/README.md). This is development tooling, not selection of DALEELAK's runtime government assistant or speech provider. No OpenRouter inference call has been made and no API key has been collected.

## Suggested worker

Recommend `qwen/qwen3-coder-next` for bounded coding tasks. [OpenRouter's model page](https://openrouter.ai/qwen/qwen3-coder-next) describes open weights, coding-agent focus, tool calling and structured output. [Public model catalog](https://openrouter.ai/api/v1/models), read 2026-10-09, lists 262,144 context and USD $0.12 per million input tokens, $0.80 per million output tokens, and $0.07 per million cache-read tokens. Actual provider route/rates may vary; record each response's actual usage/cost where available.

Example at listed uncached rates: 20,000 input and 5,000 output tokens cost approximately $0.0064 for one request. A task can require several requests; this is not a per-feature guarantee. No Kotlin/Compose accuracy or task success has been measured for this model in the project. The recommendation is a cost/scope judgment, not evidence that it is the best coding model.

## Proposed process

1. Codex defines a small task, exact allowed files, acceptance criteria and relevant constraints.
2. A separate OpenRouter worker receives only those files, the task packet and relevant instructions. Do not send the entire conversation or research archive.
3. Worker returns a patch or complete replacements as review artifacts. The orchestrator reviews and applies accepted changes; the worker does not independently publish or run arbitrary returned shell commands.
4. Preserve current UI versus assistant/state file ownership. One worker task at a time initially; use bounded output, a request/iteration cap and an explicit spending budget.
5. Report input/output tokens and cost per task. Repeated failure returns to the orchestrator instead of unlimited retries.
6. Implementation tests remain prohibited unless requested. A coding worker must inherit this requirement. Build/runtime verification remains a separate pending activity; this shell has no Java/Gradle.

The prepared Python standard-library runner uses one Chat Completions request with structured exact-substring edits. It checks paths and unique matches, then saves proposed replacements and a unified diff without editing the app. No coding CLI installation is needed. [OpenRouter tool-calling documentation](https://openrouter.ai/docs/guides/features/tool-calling) distinguishes a model's tool request from the program that executes it. The API alone does not grant file-editing access.

Keep this Codex session as the orchestrator. Do not assume its built-in model slots use an OpenRouter key. A separate worker avoids requiring a change to this session's provider. [Official Codex guidance](https://learn.chatgpt.com/docs/models) requires Responses API compatibility for a custom Codex provider; a Chat Completions-only gateway is not sufficient. This workflow can use OpenRouter's Chat Completions endpoint from its own runner.

Codex planning/review still consumes the current Codex allowance. Small task packets and short reviews are necessary to reduce that usage as well; moving worker inference does not make the orchestrator free.

## Keys and budget

Store the worker key locally through an environment variable or the prepared hidden-prompt setup script, not chat, committed code, the Android APK or the design canvas. The script saves `~/.config/daleelak/openrouter.key` with mode 0600. No key has been supplied. The runner has a suggested $1 local reservation budget, retaining $0.05 per attempt, two calls per task ID, 60 KB input and 6,000 output-token limits; actual reported cost is tracked separately. This suggested default is not a user-selected account-wide billing limit. A dedicated OpenRouter key limit is needed for account-side enforcement. No paid worker call is authorized merely by preparing this tooling.

## First task and next action

Prepared `checklist-accessibility.json` allows changes only to `features/journey/JourneyScreen.kt`: make the requirement label/row one accessible checkbox target, while preserving progress and completed-operation disabled state. This is an orchestrator-selected first task, not an additional user feature request. Other-agent file ownership is unchanged. Configure the key in the user's terminal, then agree the first bounded request; Codex runs it, reviews the diff against baseline hashes and applies accepted edits. No runner execution, tests, API inference or native build occurred during setup.

## Design usage record

Superdesign's successful Gemini 3 Flash draft reported 5 credits consumed. Its first attempt failed HTML validation; failed-attempt billing was not inspected. The generated preview drifted into another service and invented document requirements. Those defects are corrected with direct HTML edits/import, which the CLI documents as using no generation credits. Hold further paid design-generation calls while deciding the budget/workflow. The native Kotlin code did not contain those invented procedure facts.
