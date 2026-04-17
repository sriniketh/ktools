# Share Json Instance in PrettyPrint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Extract the shared `Json { prettyPrint = true }` instance in `PrettyPrint.kt` to avoid creating a new one per function call.

**Architecture:** Move the `Json` instance to a file-level `private val`, referenced by both `prettyPrintJson` and `prettyPrintCookieHeader`. This matches the pattern already used in `AboutOption.kt:10`.

**Tech Stack:** kotlinx-serialization-json

---

### File Structure

- Modify: `src/commonMain/kotlin/dev/sriniketh/PrettyPrint.kt` — extract shared Json instance

---

### Task 1: Extract shared Json instance

**Files:**
- Modify: `src/commonMain/kotlin/dev/sriniketh/PrettyPrint.kt`
- Test: `src/commonTest/kotlin/dev/sriniketh/PrettyPrintTest.kt` (existing — no changes needed)

- [x] **Step 1: Run existing tests to establish baseline**

Run: `./gradlew allTests`
Expected: All tests PASS.

- [x] **Step 2: Refactor PrettyPrint.kt to share the Json instance**

Replace the entire content of `src/commonMain/kotlin/dev/sriniketh/PrettyPrint.kt` with:

```kotlin
package dev.sriniketh

import io.ktor.http.parseClientCookiesHeader
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private val prettyJson = Json { prettyPrint = true }

/**
 * Pretty prints given json string.
 *
 * @param[jsonString] JSON string that needs to be formatted.
 * @return Formatted json string.
 * @throws IllegalArgumentException
 * @throws SerializationException
 */
@Throws(IllegalArgumentException::class, SerializationException::class)
fun prettyPrintJson(jsonString: String): String {
    val jsonElement = prettyJson.decodeFromString<JsonElement>(jsonString)
    return prettyJson.encodeToString(jsonElement)
}

/**
 * Pretty prints given Cookie header value.
 *
 * @param[cookieHeader] Cookie header that needs to be formatted.
 * @return Formatted Cookie header.
 */
fun prettyPrintCookieHeader(cookieHeader: String): String {
    val cookiesMap = parseClientCookiesHeader(cookieHeader)
    return prettyJson.encodeToString(cookiesMap)
}
```

- [x] **Step 3: Run all tests**

Run: `./gradlew allTests`
Expected: All tests PASS with identical output.

- [x] **Step 4: Run detekt**

Run: `./gradlew detektMetadataCommonMain`
Expected: No new violations.

- [x] **Step 5: Commit**

```bash
git add src/commonMain/kotlin/dev/sriniketh/PrettyPrint.kt
git commit -m "refactor: share Json instance across PrettyPrint functions"
```
