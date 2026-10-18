# uuid

Generates random UUIDs (v4 default, v7 optional) in lower or upper case, and inspects any UUID for version, variant and timestamp.

## Sub-features

- `uuid-v4` default random UUID.
- `uuid-v7-upper` v7 with `--version 7 -c upper`.
- `uuid-inspect-text` human-readable inspect.
- `uuid-inspect-json` `--json` inspect with timestamp for v1/v6/v7 and `null` otherwise.
- `uuid-inspect-invalid` bad input and bad `--version`.

## How to get to it (user POV)

- `ktools uuid [-c lower|upper] [--version 4|7]`
- `ktools uuid inspect [--json] <uuid>`

## Driving it with verify-ktools

Preconditions: doctor prints `OK`.

- **Default.** `drive.sh uuid v4 -- uuid`. Exit 0; stdout is one lowercase UUID whose third group starts with `4`.
- **V7 upper.** `drive.sh uuid v7 -- uuid --version 7 -c upper`. Exit 0; stdout is uppercase with third group starting `7`.
- **Inspect v4.** `drive.sh uuid insp4 -- uuid inspect 6bb84a9e-f77b-4189-9b41-3afb9e682887`. Output is four lines: `version: 4`, `variant: RFC 4122`, `timestamp: n/a`, `raw bytes: 6bb84a9ef77b41899b413afb9e682887`.
- **Inspect input forms.** Upper case and `urn:uuid:` prefixed input are accepted; `raw bytes` is always lower-case hex. `uuid inspect 00000000-0000-0000-0000-000000000000` gives `version: 0`, `variant: NCS`.
- **Inspect v4 JSON.** `uuid inspect <v4 uuid> --json` emits `"timestamp":null`.
- **Inspect v7 JSON.** `drive.sh uuid insp7 -- uuid inspect 018f3e8e-7b1a-7c3e-8a1b-123456789abc --json`. Stdout is `{"version":7,"variant":"RFC 4122","timestamp":"2024-05-03T13:04:36.122Z","rawBytes":"018f3e8e7b1a7c3e8a1b123456789abc"}`.
- **Inspect v1 and v6.** `uuid inspect c232ab00-9414-11ec-b3c8-9f6bdeced846 --json` and `uuid inspect 1ec9414c-232a-6b00-b3c8-9f6bdeced846 --json` both report `"timestamp":"2022-02-22T19:22:22Z"` with versions 1 and 6.
- **Invalid UUID.** `drive.sh uuid inspbad -- uuid inspect not-a-uuid`. Exit 1; stdout empty; stderr `Illegal argument exception: invalid UUID not-a-uuid`.
- **Invalid version.** `drive.sh uuid ver5 -- uuid --version 5`. Exit 1; stderr `Error: invalid value for --version: version must be 4 or 7`.
- **Round trip.** Feed the output of the default step into `uuid inspect`; it reports version 4.

## Gotchas

- Random values differ per run; assert shape only.
- `--version` on `uuid` is the UUID version; the top-level `ktools --version` is the app version (see [meta](./meta.md)). `uuid -v` is not accepted.
- Options placed before `inspect` (`uuid -c upper inspect ...`) are ignored by inspect.
