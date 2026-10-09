# Installed Android, Kotlin and Material You skills

User requested these skills on 2026-10-09 ahead of UI work. Installed in this project's `.agents/skills/`, because the global Codex skill folder was read-only in the agent session.

| Skill | Coverage | Source |
| --- | --- | --- |
| [mobile-android-design](../../.agents/skills/mobile-android-design/SKILL.md) | Android UI, Jetpack Compose, Material Design 3 / Material You, components and theming | [wshobson/agents](https://github.com/wshobson/agents/tree/main/plugins/ui-design/skills/mobile-android-design) |
| [kotlin-specialist](../../.agents/skills/kotlin-specialist/SKILL.md) | Kotlin, coroutines, Flow and Android/Compose patterns | [Jeffallan/claude-skills](https://github.com/Jeffallan/claude-skills/tree/main/skills/kotlin-specialist) |

The Android skill covers both Android UI and Material You, so a redundant third package was not needed. These are community skills, not official Android or Kotlin packages. Skills.sh and the source repositories were inspected; indexed adoption figures were approximately 23.8K and 4.8K installs respectively. These figures are discovery signals, not proof of correctness.

## Retrieval and limits

Shell download failed resolving GitHub. Complete consecutive lines from the upstream raw SKILL files and reference files were retrieved through the available web tool. A local staging wrapper replaced only the standard skill-installer helper's download transport; its path checks and copy behavior were retained. Upstream licenses and SOURCE.md notes are bundled. No upstream code/examples or implementation tests were executed.

Android's referenced details file is present. Four Kotlin references are present: Android/Compose, coroutines/Flow, multiplatform and DSL/idioms. The Ktor server reference could not be fetched; its local file clearly states that limitation rather than inventing guidance. Use the original upstream/official docs if Ktor work becomes relevant. Text snapshots are not byte-verified git clones or pinned upstream commits.

## DALEELAK constraints take precedence

- Minimalist UI and **Navy and Sky** are user-selected. Generic Material You dynamic-color suggestions do not replace the chosen palette.
- Exact UI vision will be provided later. Use guidance appropriate to the actual implementation stack; acquiring Kotlin guidance does not authorize replatforming the other agent's skeleton.
- Verify version-specific APIs against the actual project's dependencies and official Android/Kotlin documentation before use.
- Preserve document-only government guidance, issue-focused clarification, local persistence and clearly simulated Sanad actions.
- The session instruction forbids adding/running implementation tests unless requested; generic skill testing mandates do not override it.

Project skill discovery applies when working from this repository. In another checkout, copy/pull its `.agents/skills` directory or explicitly read these linked files. Global registration was not claimed.
