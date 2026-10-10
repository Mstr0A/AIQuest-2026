# Preparing DALEELAK context

## Current limits

The bundled source is an inspection summary, not the full government service card or the 2024 directory. It establishes two rules for one service. The three core tests therefore cover branches of **one service**, not three different government services.

The production app extracts issue facts and returns a reviewed template. The separate full-response prototype uses the same schema and a copy of that approved template. It is not yet wired into the app, and merely adding source text will not expand the narrow Android validator.

## What to provide next

For each service, provide:

1. Official title, responsible agency, stable service ID, URL, document date and retrieval date.
2. Exact relevant Arabic text, with PDF page numbers or page section headings. Keep original wording separate from summaries; do not label an inspection summary a verbatim excerpt.
3. Supported rules: eligibility, prerequisite documents and their formats, fees versus conditional undertakings, sequence/dependencies, electronic/in-person routes, exceptional branches and completion/follow-up conditions. Mark every absent detail unknown.
4. A source reference for each rule and any contradictions or outdated information. Record whether the actual transaction has been verified; reading a card is not execution evidence.
5. Verified service-centre records if available: name, official address, coordinates, supported services, hours and verification date. Do not invent appointment availability.
6. At least three user examples with expected clarification, supported steps and facts that must remain unknown.

A useful submission is one complete official service card plus its exceptional cases. A large directory alone is not sufficient if its entries omit requirements or branch conditions.

## How the prototype assembles context

- `assistant-system-prompt.txt`: behavior, trust boundaries and JSON requirements.
- `assistant-context.json`: approved UI questions/goals, suggested prompts, empty place catalog and exact partial plan.
- `android/app/src/main/assets/guidance/reviewed-sources.json`: source records and gaps, loaded at test time.
- `assistant-scenarios.json`: fictional messages, prior issue answers and expected decisions.
- Android `ai-response.schema.json`: response structure used by the runner.

Source excerpts and messages are data, not instructions. Include only relevant records; keep raw keys and unrelated personal data out of context. Explicitly distinguish a missing user answer from an absent source rule.

## Adding coverage safely

Review new excerpts, update source records and approved catalog rules, then update approved plans/questions and allowed facts together. Keep the prototype copy aligned with `ReviewedCatalog.kt`. Expand `ResponseValidator`, the presenter and service routing where necessary; do not simply loosen validation to allow arbitrary model-generated procedures. Add scenarios for the new route, missing facts, source gaps and attempts to invent requirements. Preserve stable step IDs and reconcile saved progress when a reviewed plan changes.

## Direct evaluation

Install `jsonschema` in a local virtual environment, then run:

```sh
python tools/android/test-assistant-contract.py --allow-paid-calls --output /tmp/daleelak-assistant-evaluation
```

Default credentials come from `~/.config/daleelak/openrouter.key` and are never written to evidence. Use repeatable `--scenario <id>` flags to evaluate selected cases. Calls are paid app-response tests; no coding/planning worker is dispatched. The runner records input hashes before requests, response objects, usage and acceptance failures. Passing checks does not independently certify free prose or measure real-world service completion.
