---
name: verify-ktools
description: Drive the real ktools native CLI (uuid, unixtime, hash, encode, decode, prettyprint) the way a user does and capture proof. Use after changing any command, option, or output in src/, or whenever a claim about CLI behavior needs evidence beyond unit tests.
---

# Verify ktools

ktools is a Kotlin Multiplatform CLI compiled to a native executable. There is no server, so "launch" means build the binary once; each drive is a fresh process. Read `features/README.md` first, then the matching feature file.

## Launch

```bash
BIN=$(.claude/skills/verify-ktools/scripts/launch.sh)   # prints the binary path; ready when it does
```

Builds `linkDebugExecutable<HostTarget>` (macOS ARM64/x64, Linux x64). Windows is not covered. It restores `src/nativeMain/resources/librariesandlicenses.json`, which the build regenerates. Needs JDK 17+. Rebuild after every source edit.

## Doctor

```bash
.claude/skills/verify-ktools/scripts/doctor.sh
```

Read-only. Fails if the binary is missing, will not run, or is older than `src/`. A stale binary proves nothing about your change. Run it first whenever results look wrong.

## Drive

```bash
.claude/skills/verify-ktools/scripts/drive.sh <feature-id> <step> -- <ktools args...>
```

Runs the binary, prints command, exit code, stdout and stderr, and saves them as `build/verify-evidence/<feature-id>/<step>.{cmd,stdout,stderr,exit}`. Plain `$BIN args` also works for exploration.

Run commands from bash. In zsh an unquoted `$args` variable is not word-split and Clikt reports `no such subcommand`.

## Evidence

- Exercise the real binary through its argv, never internal functions or test-only paths.
- Capture command, stdout, stderr and exit code for every step; `drive.sh` does this.
- Check exit codes, not only text: errors (bad base64, bad JSON, missing file) must exit 1.
- File input is a real side-effect path: create a temp file, hash it with `-f`, compare with `-s` on the same content.
- Time-dependent commands (`nowmillis`, `nowiso`, random uuid) are asserted by shape and range, not exact value.
- Evidence lives in `build/verify-evidence/` (gitignored) and is never removed by cleanup.

## Cleanup

The binary exits after every drive, so nothing of ktools runs in the background. `launch.sh` leaves a Gradle daemon that exits on its own when idle; run `./gradlew --stop` only if you started it just for this run. Delete only temp files you created (e.g. `/tmp/kt-verify-*`). Leave `build/verify-evidence/`. Run `git status`: the tree should be clean apart from your own edits.

## Isolation

Each run is an independent process with no shared state, so runs can be parallel. Use distinct feature-id/step names, or the evidence files of concurrent runs overwrite each other.
