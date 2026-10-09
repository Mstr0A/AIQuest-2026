# Voice input and simulated Sanad actions

Current handoff, 2026-10-10: read [Jordanian speech research and Waydroid verification](speech-research-2026-10-10.md). Android dictation and narrow runtime OpenRouter routing were implemented under U-047; the goal is now paused. U-049 requires dialect/provider research and device verification before further implementation. The proposals and no-test statements below describe the earlier 2026-10-09 documentation stage.

Latest user clarification, 2026-10-09: retain Sanad-dependent features, including booking, as interactive placeholders returning fake responses for the demo. Real integrations remain future. Dictation should accept everyday Jordanian conversation, including Arabic, English and mixed speech.

## Proposed dictation pipeline

1. The user taps the microphone and records a short utterance.
2. Stopping sends the recorded clip through the app's AI-provider proxy for transcription.
3. The transcript populates an editable composer; the user can correct or re-record it.
4. Send uses the same chat endpoint, reviewed context and JSON response contract as typed input.

This separates speech recognition from procedural reasoning. The chat schema need not change just because input was spoken. The transcription adapter can return `{ "text": "..." }`; language metadata, when available, is optional app input, not a government fact. Recording, transcribing, ready and error are composer states, not operation progress.

Use a short-recording interaction for the first build. Provider selection, language hints and actual microphone/browser behavior remain implementation decisions. Do not claim realtime dictation, translation or spoken assistant replies unless built. Do not save audio to local storage by default; local operation history can retain the user-approved text.

## Current documentation and recommended candidate

Official OpenAI documentation recommends `gpt-transcribe` for new general-purpose file transcription and describes prompt, keyword and multiple-language hints. This is a candidate if the team chooses OpenAI, not a user-selected provider. Source: [file transcription guide](https://developers.openai.com/api/docs/guides/speech-to-text), relevant sections read 2026-10-09. Account access and performance on the team's Jordanian mixed speech were not verified.

For this use, request a faithful transcript preserving the original language/script of words, without answering the speaker or translating. Supply short context about Jordanian service queries, with only relevant terms such as دفتر العائلة and Sanad. Arabic/English hints are a proposed configuration; confirm the selected endpoint's supported codes before implementation. A hint is not an accuracy guarantee and must not cause unspoken terms to appear.

Illustrative mixed utterance: “ضاع دفتر العيلة، وبدي أعرف الـ requirements، بقدر أعملها online؟”. This is a design example, not an observed recognition result.

Browser `SpeechRecognition` is an alternative, but MDN documents limited browser support and server-based recognition in some browsers. It should not be assumed universally available, local, or reliable on the target mixed speech. Source: [MDN SpeechRecognition](https://developer.mozilla.org/en-US/docs/Web/API/SpeechRecognition), relevant sections read 2026-10-09. Keep typed input available regardless of dictation implementation.

No audio was recorded, transmitted or evaluated in this documentation task. Jordanian dialect and code-switching accuracy remain an explicit empirical gap, rather than an advertised result.

## Placeholder action architecture

The app maps visible demo controls to local handlers. It does not require the chat model to generate booking responses or claim official success. Existing chat-source requirements must stay separate from optional demo controls; a simulated booking is not proof that the real service requires or supports booking.

Example **local fake response**, not an official API contract:

```json
{
  "mode": "demo",
  "action": "book_appointment",
  "outcome": "confirmed",
  "reference": "DEMO-BOOKING-001",
  "message": "تم تأكيد الموعد التجريبي. لم يتم إنشاء موعد رسمي."
}
```

Any centre/slot supplied by the handler is fictional or verified reference data explicitly distinguished from live availability. Store the simulated event locally with its mode; do not turn it into a government-confirmed progress status. Other Sanad-dependent controls use the same small pattern as needed by the demo, without building a general integration framework.

The user has authorized placeholders, not actual transactions. All runtime external calls can remain to the selected AI provider; placeholders make no Sanad API call.
