# encode and decode

Encodes text to utf8 hex or base64 and decodes it back, rejecting invalid input.

## Sub-features

- `encode-base64`, `encode-utf8`
- `decode-base64`
- `decode-invalid` bad base64

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
- **Invalid.** `drive.sh codec bad -- decode base64 -s '!!!'`. Exit 1; `Invalid base64 input: !!!`.
- **Round trip.** Decode the encode output of a non-ASCII string and compare.

## Gotchas

- Single-quote `!!!` so bash history expansion does not interfere.
- `encode utf8` emits hex bytes, not text.
