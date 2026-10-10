#!/usr/bin/env bash
set -euo pipefail

repository_root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)
development_root=${DALEELAK_DEV_ROOT:-"$HOME/.local/share/daleelak-dev"}
if [[ ! -x "$development_root/gradle-9.3.1/bin/gradle" ]]; then
    echo 'Local DALEELAK build tools are missing; see tools/android/README.md.' >&2
    exit 1
fi
if [[ $# -eq 0 ]]; then
    set -- :app:assembleDebug
fi
cd "$repository_root/android"
# Keep the original debug keystore so updates preserve installed app data.
env -u ANDROID_SDK_ROOT -u ANDROID_SDK_HOME \
    JAVA_HOME="$development_root/jdk" \
    ANDROID_HOME="$development_root/sdk" \
    ANDROID_USER_HOME="$development_root/android-user" \
    GRADLE_USER_HOME="$development_root/gradle-cache" \
    "$development_root/gradle-9.3.1/bin/gradle" --no-daemon --console=plain "$@"
