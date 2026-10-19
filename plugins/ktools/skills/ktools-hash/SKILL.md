---
name: ktools-hash
description: Use when the user asks for the MD5, SHA-1, SHA-256 or SHA-512 hash, digest or checksum of a string or a file, or wants to check a downloaded file against a published checksum, for example "sha256 of this file", "md5 of 'hello'" or "does this download match its checksum".
compatibility: Needs the ktools CLI on PATH (https://github.com/sriniketh/ktools/releases). Runs on macOS, Linux and Windows.
---

# ktools hash

Core principle: compute digests with `ktools hash`, and hash exactly the bytes the user means. A digest you did not
compute is a guess, and a stray trailing newline gives a different digest.

## Steps

1. Check that ktools is installed: `ktools --version`. If the command is not found, tell the user ktools is missing
   and where to get it (https://github.com/sriniketh/ktools/releases), then stop or use a tool they already have.
2. Choose the algorithm: `MD5`, `SHA1`, `SHA256` or `SHA512` (case does not matter). If the user does not say,
   use `SHA256`.
3. Hash the input:
   - File: `ktools hash SHA256 -f path/to/file`. Prefer this for anything already on disk, including binaries.
   - String: `ktools hash SHA256 -s 'hello'`. Single-quote the string so the shell does not change it.
   The digest is on the line that starts with `hash: `.
4. To check a published checksum, compare the full digest with the expected value, ignoring case. Say whether it
   matches; on a mismatch, show both values.
5. If ktools exits 1 with `IOException`, the file is missing or is a directory. Report the error.

## Gotchas

- `-s` hashes the string as UTF-8 without a trailing newline. That matches `printf '%s' 'hello' | shasum -a 256`,
  not `echo hello | shasum -a 256`.
- The empty string is rejected (`string must be non-empty`). Its SHA-256 is
  `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855` if the user needs it.
- Pass exactly one of `-s` and `-f`.

## When not to use

- Password hashing or anything that needs a salt or key (bcrypt, scrypt, Argon2, HMAC). ktools only computes plain
  digests; MD5 and SHA-1 are not safe for security uses.
- Hashing a whole directory. Hash each file, or use the user's archive or checksum tool.

## Finish

You report the algorithm, the input (file path or string) and the digest exactly as ktools printed it.
