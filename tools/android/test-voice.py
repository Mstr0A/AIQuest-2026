#!/usr/bin/env python3
"""Run explicitly opted-in live Android speech checks; never embed the runtime key."""
import argparse
import json
import re
import subprocess
import urllib.error
import urllib.request
from pathlib import Path


def usage(key):
    request = urllib.request.Request("https://openrouter.ai/api/v1/key",
                                    headers={"Authorization": f"Bearer {key}"})
    try:
        with urllib.request.urlopen(request, timeout=20) as response:
            return float(json.load(response)["data"]["usage"])
    except (urllib.error.URLError, KeyError, ValueError):
        return None


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--allow-paid-calls", action="store_true", help="Required: tests spend app API credits")
    parser.add_argument("--device", default="192.168.240.112:5555")
    parser.add_argument("--key-file", type=Path, default=Path.home() / ".config/daleelak/openrouter.key")
    parser.add_argument("--test", default="com.a0.daleelak.VoiceIntegrationTest")
    args = parser.parse_args()
    if not args.allow_paid_calls:
        parser.error("Pass --allow-paid-calls to authorize live app speech tests")
    key = args.key_file.read_text().strip()
    if not re.fullmatch(r"[a-zA-Z0-9_-]+", key):
        parser.error("Credential file must contain one valid OpenRouter key")
    repository = Path(__file__).resolve().parents[2]
    adb = ["adb", "-s", args.device]
    for apk in ("android/app/build/outputs/apk/debug/app-debug.apk",
                "android/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"):
        subprocess.run(adb + ["install", "-r", str(repository / apk)], check=True)
    subprocess.run(adb + ["shell", "pm", "grant", "com.a0.daleelak", "android.permission.RECORD_AUDIO"], check=True)
    before = usage(key)
    print("Running live app speech checks; runtime credential supplied privately.", flush=True)
    result = subprocess.run(adb + ["shell", "am", "instrument", "-w", "-r",
                            "-e", "allow_paid_voice_tests", "true", "-e", "runtime_key", key,
                            "-e", "class", args.test,
                            "com.a0.daleelak.test/androidx.test.runner.AndroidJUnitRunner"],
                            capture_output=True, text=True)
    output = (result.stdout + result.stderr).replace(key, "[redacted]")
    Path("/tmp/daleelak-voice-tests.log").write_text(output)
    print(output, flush=True)
    after = usage(key)
    cost = round(after - before, 10) if before is not None and after is not None else None
    Path("/tmp/daleelak-voice-test-cost.json").write_text(json.dumps({"usage_delta_usd": cost}))
    if cost is not None:
        print(f"OpenRouter key usage change: ${cost:.8f} (may include concurrent key activity)")
    passed = re.search(r"OK \(\d+ tests?\)", output) is not None
    return 0 if result.returncode == 0 and passed else 1


if __name__ == "__main__":
    raise SystemExit(main())
