---
name: ktools-uuid
description: Use when the user asks to generate a UUID or GUID (random v4 or time-ordered v7, lower or upper case), or to inspect, decode or validate an existing UUID to find its version, variant or embedded timestamp (v1, v6, v7), for example "give me a uuid", "make 3 uuidv7s", "when was this uuid created" or "is this a valid uuid".
compatibility: Needs the ktools CLI on PATH (https://github.com/sriniketh/ktools/releases). Runs on macOS, Linux and Windows.
---

# ktools uuid

Core principle: generate and inspect UUIDs with `ktools uuid` instead of writing one by hand or guessing what a UUID
encodes. A UUID you type yourself is not random, and a timestamp you read off by eye is often wrong.

## Steps

1. Check that ktools is installed: `ktools --version`. If the command is not found, tell the user ktools is missing
   and where to get it (https://github.com/sriniketh/ktools/releases), then stop or use a tool they already have.
2. To generate, run one command per UUID:
   - `ktools uuid` prints a random lower-case v4 UUID.
   - `ktools uuid --version 7` prints a time-ordered v7 UUID. Prefer v7 for database keys that should sort by
     creation time.
   - Add `-c upper` for upper case.
   - For several UUIDs, run the command several times (for example `for i in 1 2 3; do ktools uuid; done`).
3. To inspect, run `ktools uuid inspect <uuid>`, or `ktools uuid inspect --json <uuid>` when you need to parse
   the result. It accepts upper case and the `urn:uuid:` prefix, and reports `version`, `variant`, `timestamp`
   (ISO 8601 UTC for v1, v6 and v7; `n/a`, or `null` in JSON, otherwise) and `raw bytes`.
4. If inspect exits 1 with `invalid UUID`, the input is not a UUID. Tell the user; do not try to fix it up.

## Gotchas

- `--version` on `uuid` is the UUID version. Only 4 and 7 are accepted; `ktools --version` prints the app version.
- Options go on the command they belong to: `ktools uuid inspect --json <uuid>`, not `ktools uuid --json inspect`.

## When not to use

- The user wants UUID code in their program (for example `java.util.UUID.randomUUID()`). Write the code; do not
  shell out.
- Other ID formats (ULID, nanoid, Snowflake IDs). ktools does not generate or decode them.

## Finish

You have run ktools and copied its output exactly. For inspect, you report the fields it printed.
