# hash

Prints the MD5, SHA1, SHA256 or SHA512 digest of a string or a file.

## Sub-features

- `hash-string` digest of `-s`.
- `hash-file` digest of `-f`, equal to the string digest of the same bytes.
- `hash-errors` missing input and missing file.

## How to get to it (user POV)

- `ktools hash <ALGO> -s <text>`
- `ktools hash <ALGO> -f <path>`

## Driving it with verify-ktools

Preconditions: doctor prints `OK`.

- **String.** `drive.sh hash sha256 -- hash SHA256 -s hello`. Exit 0; stdout contains `hash: 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824`.
- **MD5.** `drive.sh hash md5 -- hash MD5 -s hello`. Hash `5d41402abc4b2a76b9719d911017c592`.
- **File.** `printf hello > /tmp/kt-verify-hash.txt`, then `drive.sh hash file -- hash SHA256 -f /tmp/kt-verify-hash.txt`. Same SHA256 as the string step (no `input string:` line). Remove the temp file after.
- **Other algorithms.** `hash SHA1 -s hello` gives `aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d`; `hash SHA512 -s hello` starts `9b71d224bd62f378`.
- **No input.** `drive.sh hash none -- hash SHA256`. Exit 1; `Error: must provide one of --file, --string`.
- **Missing file.** `drive.sh hash missing -- hash SHA256 -f /nonexistent`. Exit 1; stderr `IOException: No such file or directory`.
- **Directory.** `drive.sh hash dir -- hash SHA256 -f /tmp`. Exit 1; stderr `IOException: Is a directory`.
- **Both inputs.** `hash SHA256 -s hello -f <path>`. Exit 1; stderr `Error: option --file cannot be used with --string`; nothing is hashed.
- **Empty string.** `hash SHA256 -s ''`. Exit 1; stderr `Error: invalid value for --string: string must be non-empty`. The empty string cannot be hashed.
- **Bad or missing algorithm.** `hash foo -s x` exits 1 with `invalid choice: foo. (choose from md5, sha1, sha256, sha512)`; `hash -s x` exits 1 with `missing argument <algorithm>`.

## Gotchas

- Use `printf`, not `echo`, when creating file content; a trailing newline changes the digest.
- The algorithm argument is case-insensitive: `SHA256` and `sha256` give the same output, although the error lists the choices in lower case.
- Every error goes to stderr with exit 1; usage errors also print a `Usage:` line, the `IOException:` lines do not.
