# Fix Deprecated URL Constructor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Replace the deprecated `java.net.URL(...)` constructor with `URI(...).toURL()` in `build.gradle.kts`.

**Architecture:** Single-line change — swap the import and constructor call. The `URL` constructor was deprecated in Java 20 and the project uses JDK 17 for CI, but this future-proofs the build script.

**Tech Stack:** Gradle (Kotlin DSL), Java stdlib

---

### File Structure

- Modify: `build.gradle.kts:7-8` (import) and `build.gradle.kts:85-86` (usage)

---

### Task 1: Replace deprecated URL constructor

**Files:**
- Modify: `build.gradle.kts:7` (import line)
- Modify: `build.gradle.kts:86` (usage line)

- [x] **Step 1: Replace the import**

In `build.gradle.kts`, replace:

```kotlin
import java.net.URL
```

with:

```kotlin
import java.net.URI
```

- [x] **Step 2: Replace the constructor call**

In `build.gradle.kts`, replace:

```kotlin
            remoteUrl.set(
                URL("https://github.com/sriniketh/ktools/tree/main/src/$name/kotlin")
            )
```

with:

```kotlin
            remoteUrl.set(
                URI("https://github.com/sriniketh/ktools/tree/main/src/$name/kotlin").toURL()
            )
```

- [x] **Step 3: Verify the build still works**

Run: `./gradlew build`
Expected: Build succeeds with no deprecation warnings for this line.

- [x] **Step 4: Commit**

```bash
git add build.gradle.kts
git commit -m "fix: replace deprecated URL constructor with URI.toURL()"
```
