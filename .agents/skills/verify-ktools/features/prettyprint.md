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
- **Invalid JSON.** `drive.sh pp badjson -- prettyprint json -s '{bad'`. Exit 1; stdout includes `Invalid JSON:` and `JSON input: {bad`.
- **Cookie header.** `drive.sh pp cookie -- prettyprint cookie-header -s 'a=b; c=d'`. Exit 0; JSON object with `"a": "b"` and `"c": "d"`.

## Gotchas

- Output starts with an empty line; trim before comparing.
- Quote the cookie string; the `;` would otherwise split the shell command.
