---
name: ktools-unixtime
description: Use when the user asks to convert a unix epoch timestamp in milliseconds to a readable ISO 8601 date, to convert an ISO 8601 date-time to epoch milliseconds, or for the current time as epoch millis or ISO 8601 UTC, for example "what date is 1700000000000", "convert 2024-05-03T13:04:36Z to millis" or "what's the current epoch time".
compatibility: Needs the ktools CLI on PATH (https://github.com/sriniketh/ktools/releases). Runs on macOS, Linux and Windows.
---

# ktools unixtime

Core principle: do epoch arithmetic with `ktools unixtime`, never in your head. Mental conversions between epoch
values and calendar dates are easy to get wrong by hours or days.

## Steps

1. Check that ktools is installed: `ktools --version`. If the command is not found, tell the user ktools is missing
   and where to get it (https://github.com/sriniketh/ktools/releases), then stop or use a tool they already have.
2. Pick the operation and always pass its value as an option:
   - Millis to ISO: `ktools unixtime -o millistoiso --millis 1700000000000` prints `2023-11-14T22:13:20Z`.
   - ISO to millis: `ktools unixtime -o isotomillis --iso 2023-11-14T22:13:20Z` prints `1700000000000`.
   - Now: `ktools unixtime -o nowmillis` or `ktools unixtime -o nowiso`.
3. Make sure the input is in the unit ktools expects before running it:
   - `--millis` takes milliseconds. A 10-digit value is almost certainly seconds: multiply by 1000 (append `000`)
     and say that you did.
   - `--iso` takes an ISO 8601 instant with a zone, such as `2024-05-03T13:04:36Z` or
     `2024-05-03T13:04:36+05:30`. If the user gives a date without a time or zone, ask which zone they mean, or
     state the one you assumed (UTC).
4. If ktools exits 1 (`Number format exception` or `Illegal argument exception`), the input was malformed. Show the
   error and ask for a corrected value.

## Gotchas

- Output is always UTC (`Z`). Convert to another time zone only if the user asks, and say which zone.
- Without `--millis` or `--iso` ktools prompts interactively, which hangs or fails in an agent shell. Always pass
  the value.

## When not to use

- Date arithmetic beyond conversion (adding days, durations between dates, time zones other than UTC). Use the
  language or tool the user is working in.
- Epoch seconds the user wants back in seconds: convert with ktools, then divide by 1000 and say so.

## Finish

You report the value ktools printed, plus any unit or zone assumption you made.
