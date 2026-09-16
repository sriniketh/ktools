---
name: update-docs
description: Use when README.md, docs/index.md, docs/commands.md, or AGENTS.md may be out of sync with the actual code — after adding/removing/changing a CLI command or option, after changing the src/ layout or build/run tasks, before a release, or when asked to sync/refresh/update the docs
---

# Update Docs

Brings README.md, docs/index.md, docs/commands.md, and AGENTS.md back in sync with the real, current state of the code. The failure mode this guards against: editing docs from memory or from what "should" be true, instead of from what the code and build actually do right now. Every claim in these docs must be checked against a live source before it's written down.

## Doc surfaces and their source of truth

| Doc | Source of truth | What to check |
|---|---|---|
| `docs/commands.md` | Live `--help` output from a built binary | Every command/subcommand's usage block, byte-for-byte |
| `README.md` "Commands" list | `src/nativeMain/kotlin/dev/sriniketh/*Command.kt` + `Main.kt` subcommand wiring | One-line bullet per top-level command exists, wording matches its `--help` description |
| `docs/index.md` "Commands" list | Same as README | Kept in sync with README's list (intentionally duplicated) |
| `AGENTS.md` build/run/test commands | `./gradlew tasks --all` | Every command shown actually exists as a task; no invented/renamed tasks |
| `AGENTS.md` "Project Structure" tree | `find src -name "*.kt"` | New/removed/moved files reflected |
| `mkdocs.yml` nav | `docs/*.md` top-level pages | New published doc pages have a nav entry |

## Steps

### 1. Enumerate the real command tree

```bash
find src/nativeMain/kotlin -name "*Command.kt"
```

Read `Main.kt` to see how they're wired with `.subcommands()` — this is the authoritative command tree (including nesting, e.g. `uuid inspect`).

### 2. Build once, then capture live `--help` output for every command

Find the host-target run task (do **not** assume a task name — `nativeRun` does not exist; verify first):

```bash
./gradlew tasks --all | grep -i run
```

It will be `run<Config>Executable<Target>`, e.g. `runDebugExecutableMacosArm64` on Apple Silicon. Run it once to compile, then reuse the built `.kexe` directly (much faster than re-invoking Gradle per command):

```bash
./gradlew runDebugExecutableMacosArm64 -PrunArgs="--help"
BIN=$(find build/bin -path "*debugExecutable/ktools.kexe" | head -1)
"$BIN" --help
"$BIN" uuid --help
"$BIN" uuid inspect --help
"$BIN" unixtime --help
"$BIN" hash --help
"$BIN" encode --help
"$BIN" decode --help
"$BIN" prettyprint --help
```

Capture output for every command and subcommand found in step 1, not just the ones already in the docs — a new command won't be caught by only re-checking existing entries.

### 3. Diff against `docs/commands.md`

For each captured block, compare against the matching section in `docs/commands.md`. Update any that differ. Add a new section (with a `##` heading) for any command missing from the file; remove sections for commands that no longer exist.

The `ktools --version` and `ktools --about` blocks will almost always differ (version string is git-describe based; `--about` lists live dependency versions). Don't treat these as false positives — only update them if their *format* changed, not for every git-describe suffix or dependency patch bump. Refresh them opportunistically, not obsessively.

### 4. Cross-check README.md and docs/index.md command bullet lists

Both have a short "Commands" bullet list that must list exactly the top-level commands from step 1, with descriptions matching each command's `--help` summary line. These two lists are intentionally duplicated — keep them worded identically (or near-identically; docs/index.md may add a markdown link where README doesn't).

### 5. Check AGENTS.md against the real build

- Re-run every command shown in AGENTS.md's "Build & Run" / "Testing" / "Static Analysis" sections (or at minimum verify the task exists via `./gradlew tasks --all`). Replace any invented or renamed task name.
- Compare AGENTS.md's "Project Structure" tree against `find src -name "*.kt" | sort`. Add new files, remove deleted ones, keep the tree illustrative (not necessarily exhaustive) the way the existing tree already is.

### 6. Check mkdocs.yml nav

```bash
ls docs/*.md
```

If a `.md` file is meant to be part of the published site and has no `nav` entry in `mkdocs.yml`, add one. Files under `exclude_docs` (e.g. `docs/superpowers/`) are intentionally excluded — leave those alone.

### 7. Report and leave for review

Summarize what changed (`git diff --stat`) and why (stale command, renamed task, new file, etc.). Leave the changes unstaged for the user to review — this skill doesn't commit or push on its own.

## Common mistakes

| Mistake | Fix |
|---|---|
| Writing `--help` output from memory/training data instead of running the binary | Always capture it live (step 2) — Clikt wraps text at a fixed width and option ordering is easy to get subtly wrong by hand |
| Assuming a Gradle task name from AGENTS.md is correct | Verify with `./gradlew tasks --all` before using it anywhere, including inside the docs you're about to write |
| Only checking commands already documented | Also check `Main.kt`'s `.subcommands()` for commands that were added but never documented |
| Treating `--version`/`--about` output drift as a bug to chase every run | These are expected to drift; only fix if the *shape* of the output changed |
| Updating README.md but not the duplicate list in docs/index.md (or vice versa) | Step 4 checks both together |
