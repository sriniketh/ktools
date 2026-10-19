---
name: ktools-prettyprint
description: Use when the user asks to pretty print, format, indent or validate a JSON string, or to break an HTTP Cookie header into readable name/value pairs, for example "format this json", "is this valid JSON", "make this API response readable" or "what cookies are in this header".
compatibility: Needs the ktools CLI on PATH (https://github.com/sriniketh/ktools/releases). Runs on macOS, Linux and Windows.
---

# ktools prettyprint

Core principle: format and validate with `ktools prettyprint` instead of re-typing the JSON yourself. Hand-formatted
JSON silently drops or changes values, and it cannot tell you whether the input was valid.

## Steps

1. Check that ktools is installed: `ktools --version`. If the command is not found, tell the user ktools is missing
   and where to get it (https://github.com/sriniketh/ktools/releases), then stop or use a tool they already have.
2. Run the command, single-quoting the value so the shell passes it unchanged:
   - JSON: `ktools prettyprint json -s '{"a":1,"b":[1,2]}'` prints the JSON with 4-space indentation.
   - Cookie header: `ktools prettyprint cookie-header -s 'session=abc; theme=dark'` prints a JSON object of cookie
     names and values.
   For JSON that is already in a file, pass its contents: `ktools prettyprint json -s "$(cat response.json)"`.
3. If JSON exits 1 with `Invalid JSON:`, the input is not valid JSON. Show the error, which names the position, and
   point out the problem. Do not quietly fix the input and present it as the original.

## Gotchas

- The output starts with an empty line. Leave it out when you show the result.
- If the JSON contains a single quote, put it in a file and use `-s "$(cat file)"` instead of quoting it inline.
- For a cookie header, a repeated name keeps its last value, a bare name gets an empty value, and values are not
  URL-decoded.
- `-s` is required. Without it ktools prompts interactively, which hangs or fails in an agent shell.

## When not to use

- Querying or transforming JSON (selecting fields, filtering arrays). Use `jq` or the user's language.
- `Set-Cookie` response headers with attributes such as `Path` or `Expires`; `cookie-header` parses the request
  `Cookie` header format.
- YAML, XML or other formats.

## Finish

You show the formatted output ktools printed, or the exact error when the input was invalid.
