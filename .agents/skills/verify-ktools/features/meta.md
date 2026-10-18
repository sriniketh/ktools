# meta

Top-level options and error handling on the root `ktools` command: app version, about text, help, and what happens with no or unknown arguments.

## Sub-features

- `meta-version` `-v` / `--version`.
- `meta-about` `-a` / `--about`.
- `meta-help` `-h` / `--help` at the root and per command.
- `meta-errors` no arguments, unknown subcommand, unknown option.

## How to get to it (user POV)

- `ktools --version` / `ktools -v`
- `ktools --about` / `ktools -a`
- `ktools --help`, `ktools <command> --help`
- `ktools` with no arguments

## Driving it with verify-ktools

Preconditions: doctor prints `OK`.

- **Version.** `drive.sh meta ver-long -- --version` and `drive.sh meta ver-short -- -v`. Exit 0; both print `ktools version <git describe --tags>`, for example `ktools version v0.5.0-31-g98eab92`.
- **About.** `drive.sh meta about-long -- --about` and `drive.sh meta about-short -- -a`. Exit 0; identical stdout that starts `ktools is a command line application`, then `Libraries used:` with one `<name> : <id> : <version>` line per library (`Clikt : com.github.ajalt.clikt:clikt : <version>` is among them), then `Licenses:` with `Apache-2.0` and `MIT`. Assert shape, not library versions.
- **About is eager.** `drive.sh meta about-eager -- --about uuid`. Same stdout as `--about`; no UUID is generated.
- **Root help.** `drive.sh meta help-root -- --help`. Exit 0; lists options `-v, --version`, `-a, --about`, `-h, --help` and exactly six commands: uuid, unixtime, hash, encode, decode, prettyprint.
- **Command help.** `drive.sh meta help-uuid -- uuid --help`. Exit 0; starts `Usage: ktools uuid [<options>] <command> [<args>]...` and lists the `inspect` subcommand.
- **No arguments.** `drive.sh meta noargs --`. Exit 0; stdout is the root help (same as `--help`); stderr empty.
- **Unknown subcommand.** `drive.sh meta unknown -- bogus`. Exit 1; stdout empty; stderr `Error: no such subcommand bogus`.
- **Unknown option.** `drive.sh meta unknown-opt -- --bogus`. Exit 1; stderr `Error: no such option --bogus. Did you mean --about?`.
- **Root-only options.** `drive.sh meta uuid-v -- uuid -v`. Exit 1; stderr `Error: no such option -v`.

## Gotchas

- The version string comes from `git describe --tags` at build time. After new commits, rebuild with `scripts/launch.sh` before comparing it to `git describe --tags`.
- Running `ktools` with no arguments is not an error: it prints help and exits 0.
- `drive.sh` needs at least `--` after the step name, so the no-arguments step is `drive.sh meta noargs --`.
