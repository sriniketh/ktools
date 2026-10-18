# update

`ktools update` replaces the running executable with the latest GitHub release binary for its platform, after checking it against the release's `SHA256SUMS` to catch a corrupted or truncated download. `--check` only reports whether an update is available.

## Sub-features

- `update-check` `update --check`: reports up to date or available, never writes files.
- `update-replace` `update`: downloads, verifies, and swaps the executable in by rename.

## How to get to it (user POV)

- `ktools update --check`
- `ktools update`

## Driving it with verify-ktools

Preconditions: doctor prints `OK`; network access to `api.github.com` and `github.com`.

- **Help.** `drive.sh update help -- update --help`. Exit 0; lists `--check` and `-h, --help`.
- **Check on a dev build.** `drive.sh update check -- update --check`. A build from a commit at or after the latest release tag prints `ktools <describe> is already up to date (latest release: <tag>)` and exits 0.
- **Never run `update` on the launch.sh binary or an installed ktools.** It replaces the file it runs from.

### Real replace

A dev build is never older than the latest release, so build one that is. Do this in a scratch clone, never by tagging the real repo:

```bash
SCRATCH=$(mktemp -d)
git clone -q . "$SCRATCH/old" && git -C "$SCRATCH/old" checkout -q <branch> && git -C "$SCRATCH/old" tag v0.0.1
(cd "$SCRATCH/old" && ./gradlew -q linkDebugExecutableMacosArm64)   # or LinuxX64
mkdir "$SCRATCH/e2e" && cp "$SCRATCH/old/build/bin/macosArm64/debugExecutable/ktools.kexe" "$SCRATCH/e2e/ktools"
ln -s "$SCRATCH/e2e/ktools" "$SCRATCH/e2e/ktools-link"
"$SCRATCH/e2e/ktools-link" update --check   # Update available: v0.0.1 -> <tag>
"$SCRATCH/e2e/ktools-link" update           # Updated ktools v0.0.1 -> <tag>
```

Assert: exit 0; `ktools --version` now prints the release tag; `shasum -a 256 ktools` matches the line for this platform's asset in the release's `SHA256SUMS`; the file is still mode 755; the symlink still points at it; no `ktools.new` or `ktools.old` remains.

## Gotchas

- The released binary may predate `update`, so after a real replace `ktools update` fails with `no such subcommand update`. That is expected.
- Windows keeps `ktools.exe.old` after an update because a running executable cannot be deleted. The next update removes it.
