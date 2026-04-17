# Reduce HashCommand Duplication Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate the duplicated algorithm branches in `HashCommand.run()` by extracting a common dispatch pattern.

**Architecture:** Map each algorithm string to a pair of function references (file hash function, string hash function), then use a single `when` on the `Content` type. This cuts the `run()` method from ~45 lines to ~15 lines.

**Tech Stack:** Kotlin (function references)

---

### File Structure

- Modify: `src/nativeMain/kotlin/dev/sriniketh/HashCommand.kt` — refactor `run()` method
- Test: `src/nativeTest/kotlin/dev.sriniketh/HashCommandTest.kt` — existing tests validate behavior (no changes needed)

---

### Task 1: Refactor HashCommand.run() to eliminate duplication

**Files:**
- Modify: `src/nativeMain/kotlin/dev/sriniketh/HashCommand.kt:48-92`
- Test: `src/nativeTest/kotlin/dev.sriniketh/HashCommandTest.kt` (existing — no changes needed)

- [ ] **Step 1: Run existing tests to establish baseline**

Run: `./gradlew allTests`
Expected: All tests PASS.

- [ ] **Step 2: Replace the `run()` method with deduplicated version**

Replace the `run()` method in `HashCommand.kt` (lines 48-92) with:

```kotlin
override fun run() {
    val (fileHashFn, stringHashFn) = when (algorithm.lowercase()) {
        "md5" -> ::fileMD5 to ::stringMD5
        "sha1" -> ::fileSHA1 to ::stringSHA1
        "sha256" -> ::fileSHA256 to ::stringSHA256
        "sha512" -> ::fileSHA512 to ::stringSHA512
        else -> {
            echo("Unsupported option: $algorithm")
            return
        }
    }

    when (val it = content) {
        is FileContent -> printHashForFileOrExceptionIfFailure { fileHashFn(it.path, fileSystem) }
        is StringContent -> {
            echo("input string: ${it.text}")
            echo("hash: ${stringHashFn(it.text)}")
        }
    }
}
```

Note: The `::fileMD5` references require the functions to accept `(String, FileSystem)` params, which they do. The `::stringMD5` references accept `(String)`, which they do. The Kotlin compiler will infer the correct types.

- [ ] **Step 3: Run all tests to confirm behavior is unchanged**

Run: `./gradlew allTests`
Expected: All tests PASS with identical output.

- [ ] **Step 4: Run detekt**

Run: `./gradlew detektMacosArm64Main`
Expected: No new violations.

- [ ] **Step 5: Commit**

```bash
git add src/nativeMain/kotlin/dev/sriniketh/HashCommand.kt
git commit -m "refactor: deduplicate algorithm branches in HashCommand"
```
