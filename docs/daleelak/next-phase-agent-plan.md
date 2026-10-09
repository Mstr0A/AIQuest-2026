# OpenRouter agent phase proposal

Generated 2026-10-10 by GLM 5.3 through local OpenCode. This is an agent proposal, not a new user feature decision. No app code changed.

## Source-verified handoff

**Verified state:** `DaleelakViewModel` exposes `messages`, `draft`, `notice`, `operations`, `selectedId`, `currentPlan`, `suggestedPrompts`, `isResponding` (plus `send`/`savePlan`/`newConversation`/`toggleStep`/`toggleRequirement`/`setCompleted`/`reopen`/`bookDemo`/`deleteSelected`). `AssistantScreen` consumes these (no DemoCatalog reads); busy state, save gating and editable drafts are done. `JourneyScreen` implements the full card pager: overview with parallel ready steps, per-category checklists, necessity/condition/format labels, prerequisite enforcement, prev/next controls, labeled booking simulation and delete confirmation. `GuidancePlan` carries `sourceVersions: Map<String,String>` and `uncertainties`; `PlanStep.placeIds` exists but `ReviewedCatalog.places` is empty. Voice stays deferred; fixed light Navy/Sky is intentional.

**Task 1 — real source references in JourneyScreen.** Owner: `features/journey/JourneyScreen.kt` (+ `ui/components/` only). The current stub `«تفاصيل المراجع غير متاحة بعد»` is false: `operation.plan.sourceVersions` maps every `sourceId` (step and requirement level) to its reviewed version. Replace the stub with an expandable list showing source ID + version from that existing public field, styled minimistically, RTL, no invention. Optional extension requiring coordination: a catalog accessor for `ReviewedSource.title/url/accessedAt` — external agent owns `data/**`; request through work-plan.md, do not edit `ReviewedCatalog.kt`.

**Task 2 — case summary with correction action in AssistantScreen.** Owner: `features/assistant/AssistantScreen.kt` only. App-flow §2 requires a concise case summary with a correction action; today the accepted plan shows only title/summary/uncertainties/step titles. Add a summary card distinguishing user-confirmed answers from plan steps, and a «تصحيح إجابة» action that pre-fills `model.draft` and reveals typing (existing public setter); sending routes through `send()`, whose `reconcilePlan`/`PlanReconciler` already preserve unaffected progress. No ViewModel edits.

**Blockers:** place options and voice need external-agent data/permissions. No dark mode, no tests. Both tasks are independent — can run concurrently.
