# unixtime

Converts between epoch milliseconds and ISO 8601 and prints the current time in either form.

## Sub-features

- `unixtime-millis-to-iso`, `unixtime-iso-to-millis`, `unixtime-now`

## How to get to it (user POV)

- `ktools unixtime -o millistoiso --millis <n>`
- `ktools unixtime -o isotomillis --iso <iso>`
- `ktools unixtime -o nowmillis` / `-o nowiso`

## Driving it with verify-ktools

Preconditions: doctor prints `OK`.

- **Millis to ISO.** `drive.sh time m2i -- unixtime -o millistoiso --millis 0`. Exit 0; stdout `1970-01-01T00:00:00Z`.
- **ISO to millis.** `drive.sh time i2m -- unixtime -o isotomillis --iso 1970-01-01T00:00:00Z`. Exit 0; stdout `0`.
- **Now millis.** `drive.sh time nowms -- unixtime -o nowmillis`. Exit 0; a 13-digit integer within a few seconds of `date +%s`000.
- **Now ISO.** `drive.sh time nowiso -- unixtime -o nowiso`. Exit 0; stdout like `2026-10-18T07:22:59.056322Z` (UTC, microsecond fraction).
- **Bad millis.** `drive.sh time badmillis -- unixtime -o millistoiso --millis abc`. Exit 1; stdout empty; stderr `Number format exception: invalid input abc`.
- **Bad ISO.** `drive.sh time badiso -- unixtime -o isotomillis --iso 2023-11-30T08:02:33.006S`. Exit 1; stderr `Illegal argument exception: invalid input 2023-11-30T08:02:33.006S`.
- **Missing or bad operation.** `unixtime` exits 1 with `Error: missing option --operation`; `unixtime -o bogus` exits 1 with `invalid choice: bogus. (choose from nowmillis, nowiso, millistoiso, isotomillis)`.

## Gotchas

- `--millis` and `--iso` are prompted for when omitted, but the prompt does not read stdin: with piped or closed stdin it exits 1 with `Error: missing option --millis`. Always pass them.
- `nowiso` has a variable-length fractional second; match with a regex such as `^\d{4}-\d\d-\d\dT\d\d:\d\d:\d\d(\.\d+)?Z$`.
- Assert now-values by range, never exact.
