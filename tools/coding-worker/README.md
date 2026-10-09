# Scoped OpenRouter coding worker

Codex writes the task, this Python standard-library runner calls
`qwen/qwen3-coder-next`, and Codex reviews the proposed edits before applying them.
This is development tooling, separate from the app's runtime assistant provider.

## Start

In your own terminal, from the repository root:

```sh
python tools/coding-worker/configure-key.py
```

The hidden prompt saves the key to `~/.config/daleelak/openrouter.key` with mode
0600, outside Git and the Android app. Never paste a key into chat. An existing
`OPENROUTER_API_KEY` environment variable is also supported, if available to the
runner's process. Configuring a key does not send an API request.

Once the key and initial task are agreed, Codex runs:

```sh
python tools/coding-worker/worker.py tools/coding-worker/checklist-accessibility.json
```

The first task makes requirement rows accessible and tappable in JourneyScreen.
It does not change the other agent's assistant/state files. Outputs are in ignored
`.coding-worker/<run-id>/`: proposal, proposed files, unified diff, baseline hashes,
task and usage. The runner never changes live app files, executes model commands,
commits or pushes. The first live request subsequently succeeded under U-040;
see [first-run result](../../docs/daleelak/worker-first-run.md).

## Review and apply

1. Read `proposed.diff`, notes and usage; reject unrelated or incorrect changes.
2. Compare current allowed files with `baselines.json` before applying. A changed
   file requires a fresh review against the new contents; never overwrite it blindly.
3. Apply accepted changes using the orchestrator's normal file-editing tools.
4. Report what changed, actual billed cost if supplied and remaining reservation
   budget. Compile/run only according to current user instructions; do not add or
   run implementation tests without a user request. No native build is available
   in this shell yet.

## Bounded usage

- One request per invocation; at most two invocations per task ID; no auto retries.
- 60 KB maximum task/message packet, 6,000 output tokens, no entire chat/research dump.
- Providers must support the requested schema/parameters. Prompt/completion prices
  are capped at $0.12/$0.80 per million tokens, request fee capped at zero. If no
  provider matches, the request fails; the runner does not relax limits silently.
- A local $1 budget retains a conservative $0.05 reservation per attempt, including
  failures and missing-cost responses: at most 20 attempts per ledger. Reservations
  are not automatically refunded even when actual billed cost is smaller.
- `usage.cost`, when supplied, is recorded separately as actual cost. Missing cost
  is unknown, not zero. Locking prevents simultaneous workers in this checkout.
- This is a local spending guard, not an account-wide billing guarantee. For an
  enforced billing limit, set a $1 limit on a dedicated key in OpenRouter. Do not
  delete/reset the ledger to keep retrying; review costs and agree a new budget.

The first live request succeeded, both Python scripts passed syntax checks and ten
isolated mocked smoke checks passed. The proposed Kotlin patch needed two reviewer
corrections before application; native compilation/device testing remains pending.
Keep human/orchestrator review for each task.

References: [provider price/parameter routing](https://openrouter.ai/docs/guides/routing/provider-selection),
[usage and cost response](https://openrouter.ai/docs/api_reference/overview),
[structured outputs](https://openrouter.ai/docs/guides/features/structured-outputs).
