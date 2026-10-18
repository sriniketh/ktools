#!/usr/bin/env bash
# Usage: drive.sh <feature-id> <step-name> -- <ktools args...>
# Runs the binary and writes cmd/stdout/stderr/exit to build/verify-evidence/<feature>/<step>.*
set -u
F=$1; S=$2; shift 3
cd "$(git rev-parse --show-toplevel)"
K=$(ls build/bin/*/debugExecutable/ktools.kexe | head -1)
O=build/verify-evidence/$F; mkdir -p "$O"
{ printf 'ktools'; printf ' %q' "$@"; echo; } > "$O/$S.cmd"
"$K" "$@" > "$O/$S.stdout" 2> "$O/$S.stderr"; echo $? > "$O/$S.exit"
echo "== $S: $(cat "$O/$S.cmd")"; echo "exit=$(cat "$O/$S.exit")"; cat "$O/$S.stdout" "$O/$S.stderr"
