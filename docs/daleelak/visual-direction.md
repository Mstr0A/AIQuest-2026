# Visual direction — minimalist UI

**User requirement: minimalist UI.** Reaffirmed 2026-10-09. Native UI implementation has begun; the user supplies their vision incrementally. The other agent owns assistant/state integration in a separate checkout.

## Confirmed home and chat direction

- Home is the starting screen. A prominent new-operation action opens a fresh conversation.
- Separate entries open all operation history, current operations and finished operations. Current includes new and ongoing saved operations; finished means locally user-confirmed completed, not government approval.
- Counts come from saved local operations. Returning home and Android Back preserve operation data. The existing Locations destination remains available through a secondary home action.
- Chat is voice first: microphone is the main input control, text remains visible, and typing/keyboard appears on demand.
- Spoken replies are the intended default once audio output is connected. Readable text remains present, with stop/replay controls planned for actual playback.
- This UI increment does not record, transcribe or synthesize audio. Microphone and listening controls explain their pending status; typed input still works. Do not simulate active recording or playback.
- Navy and Sky is implemented as a fixed light theme for the demo. Dynamic wallpaper colors are disabled. Dark-mode design is still pending.

Recorded 2026-10-09, user directives to begin implementation with home navigation, followed by voice-first chat direction. The remaining screen arrangements will evolve with further user direction.

## Selected palette — Navy and Sky

**User-selected 2026-10-09:** Navy and Sky. This replaces the earlier Teal and Sand recommendation. [See the saved palette swatches](palette-options.svg).

| UI role | Selected color |
| --- | --- |
| Primary action | #1E3A8A |
| Main text | #0F172A |
| Page background | #F8FAFC |
| Soft selected surface | #DBEAFE |
| Optional restrained accent | #F59E0B |
| Card surface | #FFFFFF |

Teal and Sand and Olive and Cream remain historical alternatives in the swatches. These are original combinations, not official Sanad branding.

## Minimalist implementation suggestions

Use light surfaces, dark readable text and one main action color. Favor a small number of controls, clear typography, whitespace and restrained borders. Keep requirements and source detail expandable so the immediate next action remains easy to see. The warm accent is optional and can stay unused if it adds visual clutter. Color should support meaning, with text labels for progress/status.

These are suggestions consistent with the user's preference. The palette is selected; navigation, screen layout, fonts, component shapes and spacing remain subject to their forthcoming vision. Preserve requested functionality through progressive disclosure rather than deleting checklists, place options or demo actions in the name of minimalism.
