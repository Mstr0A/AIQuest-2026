# OpenRouter voice implementation

Date: 2026-10-10. Directive: U-050. The user authorizes implementation and live
app-flow tests through OpenRouter. This supersedes the earlier coding pause.

## Stack, credentials and current cost

| Layer | App model | Current listed price |
| --- | --- | --- |
| Dictation | `elevenlabs/scribe-v2` | $0.11/hour of audio |
| Spoken reply | `elevenlabs/eleven-v4-turbo` | $0.02/1,000 input characters |
| Issue classifier | `z-ai/glm-5.3-flash` | $0.15/million input tokens; $0.50/million output tokens |
| Optional richer narration | `elevenlabs/eleven-v4` | $0.04/1,000 input characters |

ElevenLabs prices above include the launch discount: **50% off through October
19, 2026, 08:00 PT**. They are not permanent prices. Catalog pricing was checked
with the public models API using `output_modalities=transcription` and `speech`.
[Launch and credential policy](https://openrouter.ai/blog/announcements/elevenlabs-on-openrouter/),
[Scribe](https://openrouter.ai/elevenlabs/scribe-v2),
[Turbo](https://openrouter.ai/elevenlabs/eleven-v4-turbo),
[classifier](https://openrouter.ai/z-ai/glm-5.3-flash).

Only a funded **OpenRouter API key** is required for this stack. No separate
ElevenLabs plan is required for preset/public voices. Default voice: `sarah`;
settings accept a different supported name or public ElevenLabs voice ID.
Private cloned/designed voices require ElevenLabs credentials through OpenRouter
BYOK. No exact public Jordanian preset has been verified.

Example at these rates: 10 minutes of transcription plus 10,000 characters of
spoken replies costs about **$0.218**, plus classifier use and account fees.

## Implemented flow

- Enter the key in the chat's AI settings. It stays in memory for this session.
- Tap the mic, speak, then finish recording. Capture is mono 16 kHz PCM/WAV,
  in memory, capped at 30 seconds and checked for near-silence before uploading.
- Scribe returns an editable draft. No forced language hint or translation;
  mixed Arabic/English stays available for review. Recording never auto-sends.
- Send the reviewed text. AI extracts issue facts; the reviewed local catalog,
  response contract and source validator still control government guidance.
- Accepted response text is synthesized as MP3 and played automatically, unless
  muted. Each written assistant message also has replay/stop controls.
- Replay caches the last clip in memory. New recording, navigation, backgrounding,
  key changes and new conversations cancel voice work. Playback files are deleted.
- Without a key, installed Android dictation remains a device-dependent fallback.
  The Waydroid image has no recognition service; cloud recording works without it.

API calls use the same bearer key: JSON `POST /api/v1/audio/transcriptions` with
`model`, base64 `input_audio` and `response_format: json`; JSON
`POST /api/v1/audio/speech` with `model`, approved `input`, `voice` and explicit
`response_format: mp3`. Requests, keys and transcripts are not logged. Network
and credential failures preserve the written draft/reply.
[STT API](https://openrouter.ai/docs/guides/overview/multimodal/stt),
[TTS API](https://openrouter.ai/docs/guides/overview/multimodal/tts).

## Verification

- Final APK assembly and the existing one-test unit suite passed.
- **Four live Android integration tests passed**, twice after the classifier fix:
  mixed Arabic/English → unsent draft → negative-report partial plan → automatic
  audio/replay; English → positive-report plan with mute; empty audio/invalid key
  preserving draft; actual microphone PCM capture and background cancellation.
- The final capture test produced 17,280 PCM bytes in 606 ms. UI automation also
  activated the key, started/stopped recording and rejected near-silence locally.
  No transcript was sent automatically. Logcat showed playback starting and no
  application crash during these successful runs.
- Three integration batches changed key usage by **$0.031157037 total** (about
  3.1 US cents). This includes the interrupted initial failure investigation;
  concurrent key use could affect the measured delta.
- Updated APK is installed in Waydroid. The UI session has AI enabled with the
  existing local key; restarting the app requires entering the key again.

Evidence: [final live test output](evidence/waydroid-2026-10-10-voice-tests.txt),
[voice ready](evidence/waydroid-2026-10-10-openrouter-voice-ready.png),
[recording](evidence/waydroid-2026-10-10-openrouter-recording.png),
[near-silence result](evidence/waydroid-2026-10-10-openrouter-silence-result.png).

The first live run exposed a classifier-format failure. The catalog reports
mandatory reasoning for GLM 5.3 Flash; the old 100-token total budget was too
restrictive. A 2,048-token cap with supported `reasoning.effort: low` resolved the
failure while preserving exact fact parsing and source validation.
[Reasoning and output budgets](https://openrouter.ai/docs/guides/best-practices/reasoning-tokens).

These synthetic speech fixtures establish functional integration, **not measured
Jordanian recognition accuracy or an authentic local accent**. Next quality check:
real Jordanian Arabic/English recordings, negation, Arabized English terms and
background noise, followed by native-speaker voice audition.

## Reproduce

From the repository root:

```sh
bash tools/android/build-local.sh :app:assembleDebug :app:assembleDebugAndroidTest
python tools/android/test-voice.py --allow-paid-calls
bash tools/android/run-phone.sh 192.168.240.112:5555
```

The opt-in runner reads the existing local credential file; override `--key-file`
or `--device` as needed. It installs both APKs and grants microphone permission.
The build helper preserves the original debug signing key and installed history.
