# Jordanian speech research and Android verification

Date: 2026-10-10. Original scope: U-049, research before further implementation.

**U-050 update:** OpenRouter launched ElevenLabs audio on October 7. The user's
preference and implementation authorization now select `elevenlabs/scribe-v2`
plus `elevenlabs/eleven-v4-turbo` through the existing OpenRouter key. No separate
ElevenLabs plan is needed for preset/public voices. This supersedes the earlier
coding pause and recommendation to obtain separate speech credentials. See
[implementation, prices and test results](openrouter-voice-implementation.md).
Natural Jordanian speech/accent quality remains unmeasured. The original research
and previous Waydroid observations below are retained as historical context.

## Recommended stack

| Layer | Recommended starting point | Reason and boundary |
| --- | --- | --- |
| Dictation | Speechmatics Enhanced, `model: "enhanced"`, `language: "ar_en"` | Explicit Arabic–English bilingual pack with Levantine coverage. Evaluate actual Jordanian clips before committing to it. |
| Spoken reply | ElevenLabs `eleven_v4`, with an auditioned Jordanian voice | Supports Arabic, English and voice cloning. Use a native Jordanian voice from the library or a team-owned recording; Voice Design is the faster experimental alternative. |
| Preset baseline | Azure `ar-JO-SanaNeural` or `ar-JO-TaimNeural` | Exact, documented Jordan locale voices. A locale label does not establish colloquial phrasing or advertising-quality delivery. |
| Government reasoning | Existing OpenRouter issue-fact classifier and validated local catalog | Keep procedural authority in the reviewed sources. Speech providers receive audio or approved reply text, never authority to invent procedures. |

**Recommendation:** audition Speechmatics plus ElevenLabs first. OpenRouter credentials do not authenticate direct requests to those providers; live evaluation requires their own credentials. No paid inference requests were made during this research.

## STT findings

- **Speechmatics:** Enhanced supports `ar_en` for mixed Arabic and English in the same file or stream. Arabic coverage includes Levantine speech. Its March 2026 launch reports 6.3% WER on its Arabic–English benchmark; that is a vendor result, not a DALEELAK or Jordan-only measurement. This is the strongest documented match to the requested input pattern. [Language packs](https://docs.speechmatics.com/speech-to-text/languages), [vendor benchmark and launch](https://www.speechmatics.com/company/articles-and-news/arabic-english-bilingual-speech-to-text).
- **Munsit:** the regional alternative has an explicit `munsit-en-ar` streaming model, based on its `munsit-2` recognizer generation. Use `language=auto` for mixed speech. Its current `/api/v1/listen` documentation says mixed-model hotwords are not applied; do not build a glossary feature assuming otherwise. Arabic dialect coverage is a provider claim; Jordanian performance remains unmeasured. [Streaming API](https://docs.munsit.com/speech-to-text/streaming), [provider overview](https://docs.munsit.com/).
- **OpenAI:** current official documentation recommends `gpt-transcribe` for file transcription. It accepts `languages: ["ar", "en"]`, contextual `prompt` and relevant `keywords`. Use transcription, not the English translation endpoint. This is a practical file-upload comparison candidate; no Jordanian bake-off was performed. [Official file transcription guide](https://developers.openai.com/api/docs/guides/speech-to-text).
- **ElevenLabs:** `scribe_v2` and `scribe_v2_realtime` support Arabic and English. Convenient if one speech vendor is preferred. General multilingual support does not prove Jordanian switching accuracy. Its April 2026 code-switching announcement specifically describes Indic–English improvements, so it cannot establish Arabic–English quality. [STT capabilities](https://elevenlabs.io/docs/overview/capabilities/speech-to-text), [upgrade scope](https://elevenlabs.io/blog/scribe-v2-just-got-an-upgrade).
- **Deepgram:** Nova-3 documents `ar-JO` recognition, but its documented `language=multi` pack lists ten languages without Arabic. Do not configure `multi` and assume it covers Jordanian–English mixing. [Models and language packs](https://developers.deepgram.com/docs/models-languages-overview).
- **Android SpeechRecognizer:** the app currently depends on an installed recognition service. It requests `ar-JO`, with language-switch extras only on Android 14+. The engine can vary between devices. This remains a useful phone fallback, not a verified code-switching model. The observed Waydroid Android 13 installation has no recognition service.

No provider can be promised to avoid every hallucination. Keep editable transcripts. Reject empty/silent recordings before submission where speech detection permits; evaluate quiet audio explicitly. Preserve negation, corrections and mixed-language terms. Do not force the entire utterance into English or silently add glossary terms that were not spoken.

## TTS findings and exact options

| Provider | Exact model or voice option | What is established |
| --- | --- | --- |
| ElevenLabs | `eleven_v4`; streaming alternative `eleven_v4_turbo`; established REST fallback `eleven_v3` | Arabic and English support; custom voices. A Jordanian voice must be auditioned. No universal Jordanian preset ID was verified. |
| Azure Speech | `ar-JO-SanaNeural` (female), `ar-JO-TaimNeural` (male) | Explicit Jordan locale presets. Lebanon alternatives: `ar-LB-LaylaNeural`, `ar-LB-RamiNeural`. |
| PlayHT / PlayAI | `PlayDialog-turbo`; `Amira-PlayAI`, `Ahmad-PlayAI`, `Nasser-PlayAI`, `Khalid-PlayAI` | These are documented Arabic presets, without verified Jordanian labels. Do not call them Jordanian voices. |
| Cartesia | Current Sonic product advertises Sonic-3.6; public Arabic sample is `Huda`, `ar-AE` | Explicit Emirati Arabic sample, not evidence of a Jordanian preset. Resolve the API model and voice IDs from its catalog before using them. |
| Munsit / Faseeh | `faseeh-v1-preview`; documented example `ar-najdi-male-2`; featured Fahad and Lama | Featured voices are Najdi/Hijazi. No Jordanian preset was established. Voice IDs are opaque; never manufacture IDs from a dialect naming pattern. |
| Hetaf | `hetaf-tts`; discover via `GET /api/v1/voices?language=ar&country=JO` | Documentation advertises Jordan male/female voices. The authenticated catalog is required for actual IDs. Regional voices are documented as Arabic-only; underlying engine and local realism were not established. |

Sources accessed 2026-10-10: [ElevenLabs models and endpoint distinctions](https://elevenlabs.io/docs/overview/models), [Azure voice list](https://learn.microsoft.com/en-us/azure/ai-services/speech-service/language-support?tabs=tts), [PlayAI Arabic presets](https://docs.play.ht/reference/groq), [Cartesia product](https://www.cartesia.ai/sonic), [Cartesia Arabic sample](https://www.cartesia.ai/languages/arabic), [Munsit voices](https://docs.munsit.com/text-to-speech/voices), [Munsit synthesis](https://docs.munsit.com/text-to-speech/synthesize), [Hetaf reference](https://www.hetaf.ai/docs).

**Regional lead:** [Ranneh](https://ranneh.net/) advertises a Jordanian voice agent and TTS demo. Search surfaced this claim, but direct web retrieval timed out and the local fetch failed DNS. No API documentation, model identifier, preset ID or quality result was verified. It is a lead to investigate, not a ready integration recommendation.

An accent and a dialect are different controls. A voice can have a Jordanian accent while reading formal Arabic. For natural Ammiya, supply reviewed colloquial wording and use a suitable native voice. Do not ask an unconstrained model to rewrite validated government requirements. No supplied advertisement was available to identify its exact provider or voice.

### Concrete ElevenLabs voice configuration

Preferred quality route: audition a native Jordanian library voice or clone a consenting team speaker. A professional clone has account/setup requirements; check those before scheduling it into the hackathon. Eleven v4 documentation supports professional clones; older v3 documentation has different clone limitations. Verify model availability in the team's account.

Quick experimental route: use [Voice Design](https://elevenlabs.io/docs/api-reference/text-to-voice/design) with `model_id: "eleven_ttv_v3"`, `guidance_scale: 5`, and this proposed description:

> A native woman from Amman, Jordan, around thirty, speaking everyday urban Jordanian Arabic. Warm, clear conversational delivery, moderate pace, natural local rhythm, and comfortable pronunciation of English terms within Arabic sentences. Clean studio recording, suitable for a helpful public-service assistant.

Use a 100–1000 character Jordanian preview script. Example, created for audition only:

> أهلين، أنا دليلك. احكيلي شو صار مع دفتر العيلة، وبساعدك نفهم المشكلة خطوة بخطوة. إذا بتحب تحكي عربي وتستخدم كلمات English بالنص، خذ راحتك. بتقدر تراجع كلامك قبل ما تبعته، وتعدّل أي كلمة.

Design returns preview `generated_voice_id` values. Listen to the alternatives, then save the selected preview through `/v1/text-to-voice` to obtain the reusable `voice_id`. A prompt does not guarantee local authenticity. Use a Jordanian listener to choose; never label an untested generated voice as verified.

## API integration patterns

Implement two small adapters: `SpeechTranscriber` and `SpeechSynthesizer`. Keep the current assistant JSON contract unchanged.

1. Capture a short clip with Android `AudioRecord`, initially mono PCM16 at 16 kHz where supported. Check microphone permission and actual input availability. Do not rely on a system recognition service for cloud capture.
2. Transcribe into the editable composer. Preserve the draft on failure. Send only after user approval through the existing text path.
3. Route user-approved text through OpenRouter fact classification, reviewed local guidance and the response validator.
4. Render accepted text immediately. Synthesize only that accepted reply, or an equivalent reviewed spoken template. Never read raw model JSON or an unvalidated response aloud.
5. Stop playback before recording; support stop/replay. Cancel stale transcription/playback when a conversation changes. A voice failure leaves text usable.

For a distributed app, keep provider secrets in a small proxy; use temporary speech credentials where supported. The existing private demo's session-only key pattern is another prototype option. Do not hardcode keys or persist recorded audio in operation history. Delete temporary clips after use. No accounts are needed for this demo.

**Speechmatics streaming:** connect to `wss://global.rt.speechmatics.com/v2/` with supported authentication. Start the session using the following configuration, stream PCM binary frames, render partials without sending them to the assistant, and finalize after the user stops:

```json
{
  "message": "StartRecognition",
  "audio_format": { "type": "raw", "encoding": "pcm_s16le", "sample_rate": 16000 },
  "transcription_config": { "model": "enhanced", "language": "ar_en" }
}
```

Current docs use `model`; `operating_point` is deprecated. The file-based alternative submits a short recording with the same model/language through the Batch API and retrieves its transcript. Prefer the existing SDK/proxy over handwritten polling. [Realtime protocol](https://docs.speechmatics.com/api-ref/realtime-transcription-websocket), [batch quickstart](https://docs.speechmatics.com/speech-to-text/batch/quickstart).

**ElevenLabs short replies:** `POST https://api.elevenlabs.io/v1/text-to-dialogue`, authenticated with `xi-api-key`, can return audio for a single voice/text pair:

```json
{
  "model_id": "eleven_v4",
  "inputs": [{ "voice_id": "YOUR_AUDITIONED_VOICE_ID", "text": "APPROVED_REPLY_TEXT" }]
}
```

Play the returned MP3 using Android's media player boundary. Keep mixed reply text mixed; do not enforce a language setting that degrades its English terms. V4 Turbo uses the dialogue WebSocket, so do not put its model ID into an unrelated TTS endpoint. [Dialogue REST API](https://elevenlabs.io/docs/api-reference/text-to-dialogue/convert), [model routes](https://elevenlabs.io/docs/overview/models).

## Minimum real-audio evaluation before claiming reliability

Use the same recordings across shortlisted STT providers. Record several native Jordanian speakers, with quiet and everyday noisy conditions. These are test scripts, not measured results:

| Spoken case | What must survive transcription |
| --- | --- |
| ضاع دفتر العيلة، شو أعمل؟ | Lost family book, not a passport or damaged document. |
| ما عندي بلاغ، can I start online؟ | Negative police-report answer and the English question. |
| عندي بلاغ فقدان، what's the next step؟ | Positive police-report answer. |
| مش ضايع، الدفتر تلف | Negation and correction from lost to damaged. |
| بدي أعمل scan وأرفع الـ PDF | Borrowed English terms; Arabic transliterations are acceptable if meaning survives. |
| وين أقرب service center، وبقدر أعملها بسند؟ | Service-center/Sanad terms, without added requirements. |
| I lost my family booklet | Fully English input. |
| Silence, fan noise, or speaker playback without user speech | No fabricated user request. |

Inspect negation, path-changing facts, omitted English spans, invented words and edit effort. Arabic spelling and borrowed-word transliteration can vary; WER alone is insufficient. Measure end-of-speech-to-transcript latency separately from model processing claims.

For TTS, audition the same reviewed Ammiya script with Azure Sana/Taim and the chosen ElevenLabs voice. Ask Jordanian listeners about accent, intonation, local phrasing, English pronunciation and whether it sounds formal or colloquial. Check spoken negation, conditions and names against the visible response. Live quality and latency remain unverified until these checks occur.

## Observed Waydroid result

- Connected device: `192.168.240.112:5555`, `lineage_waydroid_x86_64`, Android API 33. `adb connect` reported already connected.
- Ran `bash tools/android/run-phone.sh 192.168.240.112:5555`. APK install succeeded; `com.a0.daleelak/.MainActivity` launched with `Status: ok`.
- Inspected screenshots and UI XML. Home and Arabic RTL chat rendered; exactly three suggested prompts appeared. Voice-first input and optional typing were visible.
- `cmd package query-services --brief -a android.speech.RecognitionService` returned **No services found**. The microphone control was disabled, with the truthful message that recognition is unavailable. Android 14 language-switch extras cannot apply on this API 33 device.
- Sent the lost-family-book suggestion and then “لسه ما عندي بلاغ” through the UI. The local assistant asked the relevant police-report question and returned the documented partial plan with explicit source gaps. No invented fees, places or appointment availability were displayed.
- The UI displayed “إعداد AI”; no runtime key was configured for this run. This verifies the local fallback, not a new OpenRouter call. Prior U-047 live phone results remain separate evidence.
- TTS showed “استماع · قريباً”, confirming playback is still a placeholder. No DALEELAK crash was observed in the smoke flow or filtered logcat; the process remained alive.

Evidence: [voice unavailable screenshot](evidence/waydroid-2026-10-10-voice-unavailable.png), [local partial-plan screenshot](evidence/waydroid-2026-10-10-partial-plan.png). UI XML and intermediate captures are in `/tmp/daleelak-waydroid-*.xml` and `/tmp/daleelak-waydroid-*.png` for this local session.

Cloud STT would remove the recognition-service dependency. It would not by itself verify Waydroid's host microphone routing, actual speech accuracy, or TTS sound playback; those require separate audio checks.

For subsequent runs, use explicit device selection in adb commands if another phone is connected. Keep checking screenshots, UI XML and filtered logcat as implementation progresses. This research does not resume the paused implementation goal or install an overnight scheduler.
