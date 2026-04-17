# Fix Compilation Warnings Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate all compilation warnings from the ktools build (both Kotlin compiler deprecation warnings and Gradle deprecation warnings).

**Architecture:** Two independent warning categories to fix: (1) migrate `kotlinx.datetime.Clock`/`Instant` → `kotlin.time.Clock`/`Instant` across 4 files, (2) replace deprecated `exec {}` with `providers.exec {}` in `build.gradle.kts`. No behavior changes — pure API migration.

**Tech Stack:** Kotlin 2.3.10, kotlinx-datetime 0.7.1-0.6.x-compat, Gradle 8.14.4

---

### Task 1: Migrate `UnixTime.kt` from kotlinx.datetime to kotlin.time

**Files:**
- Modify: `src/commonMain/kotlin/dev/sriniketh/UnixTime.kt`

- [x] **Step 1: Update imports and verify compilation**

Replace the imports in `src/commonMain/kotlin/dev/sriniketh/UnixTime.kt`:

```kotlin
// BEFORE (lines 3-4):
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

// AFTER:
import kotlin.time.Clock
import kotlin.time.Instant
```

The rest of the file stays exactly the same — `Clock.System`, `clock.now()`, `toEpochMilliseconds()`, `Instant.fromEpochMilliseconds()`, `Instant.parse()`, and `.toString()` all exist on `kotlin.time` equivalents with identical signatures.

- [x] **Step 2: Run compilation to verify no errors**

Run: `./gradlew compileKotlinMacosArm64 --rerun-tasks 2>&1 | grep -E "^w:.*UnixTime.kt|error"`
Expected: No warnings or errors referencing `UnixTime.kt`

- [x] **Step 3: Commit**

```bash
git add src/commonMain/kotlin/dev/sriniketh/UnixTime.kt
git commit -m "refactor: migrate UnixTime.kt from kotlinx.datetime to kotlin.time"
```

---

### Task 2: Migrate `UnixTimeCommand.kt` from kotlinx.datetime to kotlin.time

**Files:**
- Modify: `src/nativeMain/kotlin/dev/sriniketh/UnixTimeCommand.kt`

- [x] **Step 1: Update import**

Replace the import in `src/nativeMain/kotlin/dev/sriniketh/UnixTimeCommand.kt`:

```kotlin
// BEFORE (line 9):
import kotlinx.datetime.Clock

// AFTER:
import kotlin.time.Clock
```

The constructor parameter `clock: Clock = Clock.System` on line 11 stays the same.

- [x] **Step 2: Run compilation to verify no errors**

Run: `./gradlew compileKotlinMacosArm64 --rerun-tasks 2>&1 | grep -E "^w:.*UnixTimeCommand.kt|error"`
Expected: No warnings or errors referencing `UnixTimeCommand.kt`

- [x] **Step 3: Commit**

```bash
git add src/nativeMain/kotlin/dev/sriniketh/UnixTimeCommand.kt
git commit -m "refactor: migrate UnixTimeCommand.kt from kotlinx.datetime to kotlin.time"
```

---

### Task 3: Migrate `UnixTimeTest.kt` from kotlinx.datetime to kotlin.time

**Files:**
- Modify: `src/commonTest/kotlin/dev/sriniketh/UnixTimeTest.kt`

- [x] **Step 1: Update imports**

Replace the imports in `src/commonTest/kotlin/dev/sriniketh/UnixTimeTest.kt`:

```kotlin
// BEFORE (lines 3-4):
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

// AFTER:
import kotlin.time.Clock
import kotlin.time.Instant
```

The `FakeClock` inner class on lines 44-46 stays the same — it implements `Clock` and returns `Instant.fromEpochMilliseconds(...)`, which are the same interface and method on `kotlin.time`.

- [x] **Step 2: Run tests to verify everything passes**

Run: `./gradlew allTests --rerun-tasks 2>&1 | grep -E "^w:.*UnixTimeTest.kt|FAIL|error"`
Expected: No warnings referencing `UnixTimeTest.kt`, no test failures

- [x] **Step 3: Commit**

```bash
git add src/commonTest/kotlin/dev/sriniketh/UnixTimeTest.kt
git commit -m "refactor: migrate UnixTimeTest.kt from kotlinx.datetime to kotlin.time"
```

---

### Task 4: Migrate `UnixTimeCommandTest.kt` from kotlinx.datetime to kotlin.time

**Files:**
- Modify: `src/nativeTest/kotlin/dev.sriniketh/UnixTimeCommandTest.kt`

- [x] **Step 1: Update imports**

Replace the imports in `src/nativeTest/kotlin/dev.sriniketh/UnixTimeCommandTest.kt`:

```kotlin
// BEFORE (lines 4-5):
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

// AFTER:
import kotlin.time.Clock
import kotlin.time.Instant
```

The `FakeClock` inner class on lines 80-82 stays the same.

- [x] **Step 2: Run tests to verify everything passes**

Run: `./gradlew allTests --rerun-tasks 2>&1 | grep -E "^w:.*UnixTimeCommandTest.kt|FAIL|error"`
Expected: No warnings referencing `UnixTimeCommandTest.kt`, no test failures

- [x] **Step 3: Commit**

```bash
git add src/nativeTest/kotlin/dev.sriniketh/UnixTimeCommandTest.kt
git commit -m "refactor: migrate UnixTimeCommandTest.kt from kotlinx.datetime to kotlin.time"
```

---

### Task 5: Fix Gradle `exec {}` deprecation warning in `build.gradle.kts`

**Files:**
- Modify: `build.gradle.kts`

- [x] **Step 1: Replace `exec {}` with `providers.exec {}`**

Replace the `getVersionNameFromGitTags()` function in `build.gradle.kts` (lines 66-73):

```kotlin
// BEFORE:
fun getVersionNameFromGitTags(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "describe", "--tags")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

// AFTER:
fun getVersionNameFromGitTags(): String {
    return providers.exec {
        commandLine("git", "describe", "--tags")
    }.standardOutput.asText.get().trim()
}
```

Also remove the unused import on line 6:

```kotlin
// REMOVE this line:
import java.io.ByteArrayOutputStream
```

- [x] **Step 2: Run build with `--warning-mode all` to verify no deprecation warnings**

Run: `./gradlew build --warning-mode all 2>&1 | grep -i "deprecat"`
Expected: No deprecation warnings

- [x] **Step 3: Commit**

```bash
git add build.gradle.kts
git commit -m "refactor: replace deprecated exec() with providers.exec() in build script"
```

---

### Task 6: Full verification — zero warnings

- [x] **Step 1: Clean build with full warning output**

Run: `./gradlew clean build --warning-mode all --rerun-tasks 2>&1 | grep -E "^w:|deprecat|warning"`
Expected: Zero Kotlin compiler warnings and zero Gradle deprecation warnings

- [x] **Step 2: Run all tests**

Run: `./gradlew allTests`
Expected: All tests pass

- [x] **Step 3: Run detekt**

Run: `./gradlew detektMetadataCommonMain detektMacosArm64Main`
Expected: No detekt violations
