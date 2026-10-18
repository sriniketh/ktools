# encode and decode

Encodes text to utf8 hex or base64 and decodes it back, rejecting invalid input.

## Sub-features

- `encode-base64`, `encode-utf8`
- `decode-base64`
- `decode-invalid` bad base64, bad hex, or bytes that are not valid UTF-8

## How to get to it (user POV)

- `ktools encode <utf8|base64> -s <text>`
- `ktools decode <utf8|base64> -s <text>`

## Driving it with verify-ktools

Preconditions: doctor prints `OK`.

- **Base64 encode.** `drive.sh codec b64enc -- encode base64 -s hello`. Exit 0; `encoded string: aGVsbG8=`.
- **Utf8 encode.** `drive.sh codec utf8enc -- encode utf8 -s hello`. Exit 0; `encoded string: 68656c6c6f`.
- **Base64 decode.** `drive.sh codec b64dec -- decode base64 -s aGVsbG8=`. Exit 0; `decoded string: hello`.
- **Utf8 decode.** `drive.sh codec utf8dec -- decode utf8 -s 68656c6c6f`. Exit 0; `decoded string: hello`.
- **Non-ASCII.** `drive.sh codec uni -- encode base64 -s "héllo ✓"`. Exit 0; `encoded string: aMOpbGxvIOKckw==`.
- **Invalid base64.** `drive.sh codec bad -- decode base64 -s '!!!'`. Exit 1; stdout is only `input string: !!!`; stderr `Invalid base64 input: !!!`.
- **Invalid hex.** `drive.sh codec badhex -- decode utf8 -s ZZZZ`. Exit 1; stderr `Invalid UTF-8 hex input: ZZZZ`. Odd-length hex (`abc`) fails the same way.
- **Invalid UTF-8 from hex.** `drive.sh codec ff -- decode utf8 -s ff`. Exit 1; stdout only `input string: ff`; stderr `Invalid UTF-8 hex input: ff`. A truncated multibyte sequence (`c3`) fails the same way.
- **Invalid UTF-8 from base64.** `drive.sh codec b64ff -- decode base64 -s /w==`. Exit 1; stdout only `input string: /w==`; stderr `Invalid base64 input: /w==` (the same message as malformed base64). `ww==` fails the same way.
- **Unpadded base64.** `decode base64 -s aGVsbG8` exits 0 with `decoded string: hello`; missing `=` padding is accepted.
- **Empty and bad format.** `-s ''` exits 1 with `string must be non-empty`; `decode md5 -s x` exits 1 with `invalid choice: md5. (choose from utf8, base64)`.
- **Round trip.** `decode base64 -s aMOpbGxvIOKckw==` and `decode utf8 -s 68c3a96c6c6f20e29c93` both print `decoded string: héllo ✓`.

## Gotchas

- Single-quote `!!!` so bash history expansion does not interfere.
- `encode utf8` emits hex bytes, not text.
- Every command echoes `input string: <s>` first, so on a decode failure stdout is not empty.
- Omitting `-s` does not read stdin: it exits 1 with `Error: missing option --string`.