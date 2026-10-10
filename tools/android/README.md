# Build and run on a phone

## Current setup — 2026-10-10

Waydroid is directly accessible at `192.168.240.112:5555`. The USB limitations
below apply to the earlier environment. From the repository root:

```sh
bash tools/android/build-local.sh :app:assembleDebug :app:assembleDebugAndroidTest
bash tools/android/run-phone.sh 192.168.240.112:5555
python tools/android/test-voice.py --allow-paid-calls
```

The build helper reuses the original local debug keystore. The opt-in test runner
reads `~/.config/daleelak/openrouter.key`, installs both APKs, grants microphone
permission and spends app API credits. It never embeds the key in either APK.
It writes sanitized results and a key-usage delta to `/tmp/daleelak-voice-tests.log`
and `/tmp/daleelak-voice-test-cost.json`. Concurrent use of that key can affect
the delta. Use `--test com.a0.daleelak.VoiceIntegrationTest#methodName` for one test.
Synthetic Arabic/English fixtures check plumbing, not real Jordanian quality.

See [implementation handoff](../../docs/daleelak/openrouter-voice-implementation.md).
The original notes below are historical.

The current agent shell is containerized, with no `/dev/bus/usb` or `/dev/kvm`.
It can download/build with network access but cannot see the selected USB phone.
Use the normal desktop terminal to install/launch after the APK is built:

```sh
bash tools/android/run-phone.sh
```

The script requires one adb-authorized phone (or an explicit serial argument),
installs the existing debug APK with `-r`, then launches MainActivity. It does not
uninstall, clear history, change system configuration or publish a release.
No successful device run is claimed until adb install/start succeeds.

Build status, 2026-10-09: `:app:assembleDebug` succeeded after correcting the icon
helper parameter name to `pathBuilder`. APK signature verification passed (v2);
package `com.a0.daleelak`, min API 26, target API 36, APK approximately 9.3 MB.
The installer passed Bash syntax checking. The user subsequently confirmed "App
opens" after running it in the normal terminal. This confirms basic launch by user
report; device walkthrough/audio checks remain pending.

User-local build tools: `~/.local/share/daleelak-dev` contains Temurin JDK 17,
Gradle 9.3.1, SDK 36.1/build tools 36.0.0, downloads and build logs. APK output:
`android/app/build/outputs/apk/debug/app-debug.apk`. The current setup/assembly
helper is `/tmp/daleelak-build-android.py`; invocation:

```sh
python /tmp/daleelak-build-android.py
```

It uses task-local environment variables; the repository's AGP/Kotlin/SDK baseline
is preserved. No global Java/Gradle installation, emulator or shell-profile change
is required by this helper. Archive downloads were checked against publisher hashes.
The SDK CLI has moved from semicolon package names to slash names in listing output;
the helper accepts either listing format.

For a manual demo, inspect: home navigation, optional typed chat, reviewed lost-book
clarification, accepted plan and documented gaps, save/resume, step cards/checklists,
current/finished history, and labeled booking simulation. Audio remains placeholder,
places empty, and the runtime assistant local/deterministic until the external agent
connects a validated provider. A build is not a device or voice-quality test.
