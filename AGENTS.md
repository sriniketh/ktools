# AGENTS.md

## Project Overview

ktools is a Kotlin Multiplatform (KMP) CLI utility providing developer tools: UUID generation, time conversions, hashing (MD5/SHA1/SHA256/SHA512), base64 encoding/decoding, and JSON pretty-printing. It compiles to native executables for macOS (ARM64/x64), Linux (x64), and Windows (x64).

## Build & Run

```bash
./gradlew build                          # Build all targets
./gradlew nativeRun -PrunArgs="uuid"     # Run locally with args
./gradlew macosArm64Binaries             # Build macOS ARM binary
./gradlew macosX64Binaries               # Build macOS x64 binary
./gradlew linuxX64Binaries               # Build Linux binary
./gradlew mingwX64Binaries               # Build Windows binary
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
│   ├── UUID.kt, Hashing.kt, EncodingDecoding.kt, UnixTime.kt, PrettyPrint.kt
│   └── helpers/File.kt
├── commonTest/kotlin/dev/sriniketh/     # Unit tests for common code
├── nativeMain/kotlin/dev/sriniketh/     # CLI commands (Clikt) and entry point
│   ├── Main.kt                          # Entry point, wires subcommands
│   ├── *Command.kt                      # One file per CLI command
│   └── models/LibrariesAndLicenses.kt
└── nativeTest/kotlin/dev.sriniketh/     # CLI integration tests
```

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
./gradlew detektMacosArm64Main           # Check common + native sources (macOS ARM64)
```

- **Detekt** (v1.23.8) is configured in `build.gradle.kts` with `detekt.yml` at the project root
- `buildUponDefaultConfig = true` — extends the default ruleset
- Generated code (`build/`, `BuildConfig.kt`) is excluded
- In KMP, the base `detekt` task has no sources — use target-specific tasks (e.g., `detektMetadataCommonMain`, `detektMacosArm64Main`)

## Dependencies

- **Clikt** — CLI argument parsing
- **kotlinx-datetime** — Cross-platform date/time
- **kotlinx-serialization-json** — JSON serialization
- **Okio** — Cross-platform file I/O (`okio-fakefilesystem` for tests)
- **Ktor Client** — HTTP client (cookie parsing in pretty-print)
- **Detekt** — Static code analysis

## Versioning

Version is derived from git tags via `git describe --tags`. Releases are triggered by pushing tags matching `v*.*.*`.

## CI

GitHub Actions runs `./gradlew build` on macOS, Ubuntu, and Windows for every push to main and on PRs. JDK 17 (Zulu) is required.
