# ktools verification map

Maintained source for verifying user-facing ktools behavior. Read this index, then the feature file for what you changed.

## Baseline preconditions

- Binary built by `scripts/launch.sh`; `scripts/doctor.sh` prints `OK`.
- Run from the repo root in bash.
- No network or data directory is needed. File inputs use temp files you create.

## Driving conventions

- Use `scripts/drive.sh <feature-id> <step> -- <args>`; arguments after `--` are passed literally to ktools.
- Quote JSON and cookie strings so the shell passes them as one argument.
- Assert exit code and exact stdout for deterministic commands.
- Errors go to stderr with exit 1, including Clikt usage errors (never 2). `drive.sh` prints both streams after `exit=`, so check the `.stdout` and `.stderr` files to tell them apart.
- Never rely on interactive prompts (`-s`, `--millis`, `--iso`): they do not read stdin and fail with `missing option`.

## Proof and skip reporting

- Each step has `.cmd`, `.stdout`, `.stderr`, `.exit` in `build/verify-evidence/<feature-id>/`.
- Report a step you could not run with the command and the unmet precondition. Do not substitute another path.
- Windows (`mingwX64`) cannot be driven from macOS/Linux; report it as unverified.

## Features

- [uuid](./uuid.md): generate v4/v7, case option, inspect with text and JSON output.
- [hash](./hash.md): MD5/SHA1/SHA256/SHA512 over strings and files.
- [encode-decode](./encode-decode.md): utf8 and base64 round trips and invalid input.
- [unixtime](./unixtime.md): millis and ISO conversion and now operations.
- [prettyprint](./prettyprint.md): JSON and cookie-header formatting and invalid JSON.
- [meta](./meta.md): top-level `--version`, `--about`, `--help`, no arguments and unknown commands.
