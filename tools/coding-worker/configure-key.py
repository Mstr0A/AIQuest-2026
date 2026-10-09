#!/usr/bin/env python3
"""Run in the user's terminal. Store a development-only key outside the repo."""
import getpass
import os
from pathlib import Path

directory = Path.home() / ".config/daleelak"
target = directory / "openrouter.key"
if directory.is_symlink() or target.is_symlink():
    raise SystemExit("Refusing a symlink credential location.")
key = getpass.getpass("OpenRouter API key (hidden): ").strip()
if not key or any(character.isspace() for character in key):
    raise SystemExit("Key must be nonempty and contain no whitespace.")
directory.mkdir(parents=True, exist_ok=True, mode=0o700)
directory.chmod(0o700)
descriptor = os.open(target, os.O_WRONLY | os.O_CREAT | os.O_TRUNC | os.O_NOFOLLOW, 0o600)
with os.fdopen(descriptor, "w") as handle:
    os.fchmod(handle.fileno(), 0o600)
    handle.write(key + "\n")
print("Saved local development credential. No API request made.")
