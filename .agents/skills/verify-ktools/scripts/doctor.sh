#!/usr/bin/env bash
# Read-only: is the built binary present, runnable, and current with HEAD sources?
set -u
cd "$(git rev-parse --show-toplevel)"
K=$(ls build/bin/*/debugExecutable/ktools.kexe 2>/dev/null | head -1)
[ -n "$K" ] || { echo "FAIL: no binary; run scripts/launch.sh"; exit 1; }
V=$("$K" --version 2>&1) || { echo "FAIL: binary does not run: $V"; exit 1; }
echo "binary: $K"; echo "$V"
echo "git describe: $(git describe --tags)"
if [ -n "$(find src build.gradle.kts -newer "$K" -type f ! -name librariesandlicenses.json 2>/dev/null | head -1)" ]; then
  echo "STALE: sources are newer than the binary; re-run scripts/launch.sh"; exit 1
fi
echo "OK"
