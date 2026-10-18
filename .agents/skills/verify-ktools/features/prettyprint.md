# prettyprint

Formats JSON and Cookie header strings into indented JSON, reporting parse errors.

## Sub-features

- `pp-json`, `pp-json-invalid`, `pp-cookie`

## How to get to it (user POV)

- `ktools prettyprint json -s <json>`
- `ktools prettyprint cookie-header -s <header>`

## Driving it with verify-ktools

Preconditions: doctor prints `OK`.

- **JSON.** `drive.sh pp json -- prettyprint json -s '{"a":1}'`. Exit 0; stdout is a blank line, then `{`, `    "a": 1`, `}`.
- **Invalid JSON.** `drive.sh pp badjson -- prettyprint json -s '{bad'`. Exit 1; stdout is only a blank line; stderr includes `Invalid JSON:` and `JSON input: {bad`.
- **Array and scalar.** `prettyprint json -s '[1,2]'` prints the array one element per line, 4-space indent; `-s 42` prints `42`. Both exit 0.
- **Empty string.** `prettyprint json -s ''`. Exit 1; stderr `Error: invalid value for --string: string must be non-empty`.
- **Cookie header.** `drive.sh pp cookie -- prettyprint cookie-header -s 'a=b; c=d'`. Exit 0; JSON object with `"a": "b"` and `"c": "d"`.
- **Cookie edge cases.** `-s 'a=b=c; a=z; ;x; e=%2F'` exits 0 with `"a": "z"` (last duplicate wins), `"x": ""` (bare name gets an empty value) and `"e": "%2F"` (not URL-decoded).

## Gotchas

- Output starts with an empty line; trim before comparing.
- Quote the cookie string; the `;` would otherwise split the shell command.
- Omitting `-s` does not read stdin: with piped or closed stdin it exits 1 with `Error: missing option --string`. Always pass `-s`.
- Usage errors (empty `-s`, missing content type) exit 1, not 2.
