---
name: ktools-encoding
description: Use when the user asks to base64-encode or base64-decode text, or to convert text to or from its UTF-8 bytes as hex, for example "base64 this string", "decode aGVsbG8=", "what does this base64 say" or "hex bytes of 'héllo'". Also use it to read the payload of a base64 value found in a config, header or log.
compatibility: Needs the ktools CLI on PATH (https://github.com/sriniketh/ktools/releases). Runs on macOS, Linux and Windows.
---

# ktools encode and decode

Core principle: encode and decode with `ktools encode` and `ktools decode` rather than by hand. Base64 and hex done
in your head go wrong on padding and on non-ASCII characters.

## Steps

1. Check that ktools is installed: `ktools --version`. If the command is not found, tell the user ktools is missing
   and where to get it (https://github.com/sriniketh/ktools/releases), then stop or use a tool they already have.
2. Run the command, single-quoting the value so the shell passes it unchanged:
   - `ktools encode base64 -s 'hello'` prints `encoded string: aGVsbG8=`.
   - `ktools decode base64 -s 'aGVsbG8='` prints `decoded string: hello`.
   - `ktools encode utf8 -s 'hello'` prints the UTF-8 bytes as hex: `encoded string: 68656c6c6f`.
   - `ktools decode utf8 -s '68656c6c6f'` turns hex bytes back into text.
   Each command first echoes `input string: <value>`; the result is on the `encoded string:` or `decoded string:`
   line.
3. If decode exits 1 with `Invalid base64 input` or `Invalid UTF-8 hex input`, the value is malformed or its bytes
   are not UTF-8 text (for example an image or compressed data). Say so; do not invent a decoding.

## Gotchas

- Decoding base64 accepts missing `=` padding and the URL-safe alphabet (`-` and `_`), so it also reads base64url.
- `utf8` means hex of UTF-8 bytes, not a character-set conversion.
- The value must be non-empty, and must be passed with `-s`. Without it ktools prompts interactively, which hangs
  or fails in an agent shell.

## When not to use

- Verifying a JWT. You can read its header or payload by decoding one dot-separated part at a time, but ktools
  does not check signatures or expiry.
- Binary files, URL encoding (percent-encoding) or HTML entities. ktools does not handle those.

## Finish

You report the encoded or decoded value exactly as ktools printed it.
