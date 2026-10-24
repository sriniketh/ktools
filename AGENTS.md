# AGENTS.md

## Project Overview

ktools is a Kotlin Multiplatform (KMP) CLI utility providing developer tools: UUID generation, time conversions, hashing (MD5/SHA1/SHA256/SHA512), base64 encoding/decoding, JSON pretty-printing, self-update from GitHub releases, and `ktools init`, which installs the bundled agent skills into AI coding agents. It compiles to native executables for macOS (ARM64/x64), Linux (x64), and Windows (x64).

## Build & Run

```bash
./gradlew build                                          # Build all targets
./gradlew runDebugExecutableMacosArm64 -PrunArgs="uuid"  # Run locally with args (host-specific; see `./gradlew tasks --all | grep run`)
./gradlew macosArm64Binaries                             # Build macOS ARM binary
./gradlew macosX64Binaries                               # Build macOS x64 binary
./gradlew linuxX64Binaries                               # Build Linux binary
./gradlew mingwX64Binaries                               # Build Windows binary
```

## Testing

```bash
./gradlew allTests                       # Run all tests (common + native)
```

Tests are in two source sets:
- `src/commonTest/` — Unit tests for pure business logic
- `src/nativeTest/` — CLI integration tests using Clikt's `.test()` method

## Project Structure

```
src/
├── commonMain/kotlin/dev/sriniketh/     # Platform-agnostic business logic (pure functions)
│   ├── UUID.kt, Hashing.kt, EncodingDecoding.kt, UnixTime.kt, PrettyPrint.kt, AboutText.kt
│   ├── SelfUpdate.kt                    # Release planning, checksum check, executable replacement
│   ├── AgentSkills.kt                   # Bundled skill parsing, install/update/uninstall planning
│   ├── SkillAgents.kt                   # Known agents and the skills directories they read
│   └── models/LibrariesAndLicenses.kt
├── commonTest/kotlin/dev/sriniketh/     # Unit tests for common code
├── nativeMain/kotlin/dev/sriniketh/     # CLI commands (Clikt) and entry point
│   ├── Main.kt                          # Entry point, wires subcommands
│   ├── AboutOption.kt                   # --about option
│   ├── SelfUpdatePlatform.kt            # expect declarations for the update command's platform hooks
│   ├── Environment.kt                   # Environment variables and home directory
│   └── *Command.kt                      # One file per CLI command
├── macosMain/, linuxMain/, mingwMain/   # actuals: executable path, chmod, release target
└── nativeTest/kotlin/dev.sriniketh/     # CLI integration tests
plugins/ktools/                          # Agent plugin for users of ktools (Claude, Codex and Agent Plugins manifests)
└── skills/<skill>/SKILL.md              # Skills bundled into the binary and installed by `ktools init`
.claude-plugin/, .agents/plugins/        # Marketplaces listing plugins/ktools
.agents/skills/                          # Skills for developing ktools itself (not shipped)
```

## Agent skills

- `plugins/ktools/skills/` is embedded into the binary as `BuildConfig.BUNDLED_SKILLS_JSON` at build time, so editing
  a skill needs a rebuild and ships with the next release. Hidden files (names starting with `.`) are not bundled.
- `ktools init` writes each skill with a `.ktools-skill` marker holding the ktools version; only directories with that
  marker are updated or removed.
- Skill conventions: `name:` equals the directory name, and `description` starts with `Use when`.
  `InitCommandTest` checks both. When a command, option or output changes, update the matching skill too.
- Plugin manifest versions (`plugins/ktools/plugin.json`, `.claude-plugin/plugin.json`, `.codex-plugin/plugin.json`)
  match the release version without the `v`; the `generate-release` skill bumps them.

## Architecture

**Functional core, imperative shell:** Pure functions live in `commonMain`, CLI wrappers in `nativeMain`.

- Business logic functions are platform-agnostic and accept `FileSystem`/`Clock` parameters for testability
- Each CLI command is a separate class extending `CliktCommand` in `nativeMain`
- Entry point in `Main.kt` composes all subcommands via `.subcommands()`

## Key Conventions

- **Kotlin style:** Official (`kotlin.code.style=official` in `gradle.properties`)
- **Package:** `dev.sriniketh`
- **Test names:** Backtick-enclosed descriptive phrases (e.g., `` `hash md5 returns hash for string input` ``)
- **File I/O:** Use Okio's `FileSystem` (inject `FakeFileSystem` in tests)
- **CLI framework:** Clikt — commands use `mutuallyExclusiveOptions()` and sealed interfaces for type-safe option groups
- **KDoc:** Public functions should have documentation comments
- **`@Throws`:** Used on functions that can throw exceptions

## Static Analysis

```bash
./gradlew detektMetadataCommonMain       # Check common sources
./gradlew detektMetadataNativeMain       # Check native sources
./gradlew detektMetadataMacosMain        # Check macOS sources
```

- **Detekt** (v1.23.8) is configured in `build.gradle.kts` with `detekt.yml` at the project root
- `buildUponDefaultConfig = true` — extends the default ruleset
- Generated code (`build/`, `BuildConfig.kt`) is excluded
- In KMP, the base `detekt` task has no sources — use source-set tasks (e.g., `detektMetadataCommonMain`, `detektMetadataNativeMain`). Per-target tasks such as `detektMacosArm64Main` report NO-SOURCE, and `linuxMain`/`mingwMain` have no detekt task

## Dependencies

- **Clikt** — CLI argument parsing
- **kotlinx-datetime** — Cross-platform date/time
- **kotlinx-serialization-json** — JSON serialization
- **Okio** — Cross-platform file I/O (`okio-fakefilesystem` for tests)
- **Ktor Client** — HTTP client (cookie parsing in pretty-print, release download in `update`). Engines per platform: Darwin (macOS), Curl (Linux), WinHttp (Windows)
- **Detekt** — Static code analysis

## Versioning

Version is derived from git tags via `git describe --tags`. Releases are triggered by pushing tags matching `v*.*.*`.

## CI

GitHub Actions runs `./gradlew build` on macOS, Ubuntu, and Windows for every push to main and on PRs. JDK 17 (Zulu) is required.
