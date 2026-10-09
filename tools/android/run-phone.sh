#!/usr/bin/env bash
set -euo pipefail

repository_root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)
apk_path="$repository_root/android/app/build/outputs/apk/debug/app-debug.apk"
adb_bin=$(command -v adb || true)
if [[ -z "$adb_bin" ]]; then
    echo 'adb is required in your normal terminal.' >&2
    exit 1
fi
if [[ ! -f "$apk_path" ]]; then
    echo 'Build APK first: android/app/build/outputs/apk/debug/app-debug.apk' >&2
    exit 1
fi
if [[ $# -gt 1 ]]; then
    echo 'Usage: bash tools/android/run-phone.sh [device-serial]' >&2
    exit 1
fi
if [[ $# -eq 1 ]]; then
    phone_serial=$1
else
    mapfile -t connected_phones < <("$adb_bin" devices | awk 'NR > 1 && $2 == "device" { print $1 }')
    if [[ ${#connected_phones[@]} -ne 1 ]]; then
        "$adb_bin" devices -l
        echo 'Connect one phone, enable USB debugging and accept the authorization prompt.' >&2
        echo 'With multiple devices, pass the desired device serial as the only argument.' >&2
        exit 1
    fi
    phone_serial=${connected_phones[0]}
fi
"$adb_bin" -s "$phone_serial" install -r "$apk_path"
"$adb_bin" -s "$phone_serial" shell am start -W -n com.a0.daleelak/.MainActivity
