# Consistent Base64 Decode API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Make `decodeFromBase64` consistent with the rest of the encoding/decoding API by throwing `IllegalArgumentException` on invalid input instead of returning null.

**Architecture:** Change `decodeFromBase64` return type from `String?` to `String`, throwing `IllegalArgumentException` when the input is invalid base64. Update the caller in `DecodingCommand` and the unit test accordingly.

**Tech Stack:** Kotlin, Okio

---

### File Structure

- Modify: `src/commonMain/kotlin/dev/sriniketh/EncodingDecoding.kt:37` — change return type and throw on null
- Modify: `src/nativeMain/kotlin/dev/sriniketh/DecodingCommand.kt:39-45` — update caller to catch exception
- Modify: `src/commonTest/kotlin/dev/sriniketh/EncodingDecodingTest.kt:33-38` — update test expectation
- Test: `src/nativeTest/kotlin/dev.sriniketh/DecodingCommandTest.kt` — verify CLI behavior (may need new test)

---

### Task 1: Update `decodeFromBase64` to throw instead of returning null

**Files:**
- Modify: `src/commonMain/kotlin/dev/sriniketh/EncodingDecoding.kt:33-37`
- Modify: `src/commonTest/kotlin/dev/sriniketh/EncodingDecodingTest.kt:33-38`

- [x] **Step 1: Write the updated test for invalid input**

In `src/commonTest/kotlin/dev/sriniketh/EncodingDecodingTest.kt`, replace the test `decodeFromBase64 returns null when provided with invalid base64 input` with:

```kotlin
@Test
fun `decodeFromBase64 throws IllegalArgumentException when provided with invalid base64 input`() {
    assertFailsWith<IllegalArgumentException> {
        decodeFromBase64("4rdHF")
    }
}
```

Add the import at the top of the file:

```kotlin
import kotlin.test.assertFailsWith
```

And remove the now-unused import:

```kotlin
// Remove: import kotlin.test.assertNull
```

- [x] **Step 2: Run the test to see it fail**

Run: `./gradlew allTests`
Expected: FAIL — `decodeFromBase64` returns null instead of throwing.

- [x] **Step 3: Update `decodeFromBase64` implementation**

In `src/commonMain/kotlin/dev/sriniketh/EncodingDecoding.kt`, replace:

```kotlin
/**
 * Decodes given base64 encoded content.
 *
 * @param[encodedContent] Content to be decoded.
 * @return Decoded string.
 */
fun decodeFromBase64(encodedContent: String): String? = encodedContent.decodeBase64()?.utf8()
```

with:

```kotlin
/**
 * Decodes given base64 encoded content.
 *
 * @param[encodedContent] Content to be decoded.
 * @return Decoded string.
 * @throws IllegalArgumentException
 */
@Throws(IllegalArgumentException::class)
fun decodeFromBase64(encodedContent: String): String {
    val decoded = encodedContent.decodeBase64()
        ?: throw IllegalArgumentException("Invalid base64 input: $encodedContent")
    return decoded.utf8()
}
```

- [x] **Step 4: Run tests to verify the common tests pass**

Run: `./gradlew allTests`
Expected: The common tests pass. The native `DecodingCommandTest` may fail if it relies on the null path — check next.

- [x] **Step 5: Commit**

```bash
git add src/commonMain/kotlin/dev/sriniketh/EncodingDecoding.kt src/commonTest/kotlin/dev/sriniketh/EncodingDecodingTest.kt
git commit -m "refactor: make decodeFromBase64 throw on invalid input instead of returning null"
```

---

### Task 2: Update DecodingCommand to handle the new exception

**Files:**
- Modify: `src/nativeMain/kotlin/dev/sriniketh/DecodingCommand.kt:38-45`
- Test: `src/nativeTest/kotlin/dev.sriniketh/DecodingCommandTest.kt`

- [x] **Step 1: Read the current DecodingCommandTest to see existing coverage**

Read: `src/nativeTest/kotlin/dev.sriniketh/DecodingCommandTest.kt`

- [x] **Step 2: Update the base64 branch in DecodingCommand.run()**

In `src/nativeMain/kotlin/dev/sriniketh/DecodingCommand.kt`, replace the `"base64"` branch (lines 38-45):

```kotlin
"base64" -> {
    echo("input string: $content")
    val decoded = decodeFromBase64(content)
    if (decoded != null) {
        echo("decoded string: $decoded")
    } else {
        echo("Invalid base64 input: $content")
    }
}
```

with:

```kotlin
"base64" -> {
    echo("input string: $content")
    try {
        echo("decoded string: ${decodeFromBase64(content)}")
    } catch (_: IllegalArgumentException) {
        echo("Invalid base64 input: $content")
    }
}
```

- [x] **Step 3: Run all tests**

Run: `./gradlew allTests`
Expected: All tests PASS. The CLI output for invalid base64 remains `"Invalid base64 input: ..."` so existing CLI tests should still pass.

- [x] **Step 4: Run detekt**

Run: `./gradlew detektMetadataCommonMain && ./gradlew detektMacosArm64Main`
Expected: No new violations.

- [x] **Step 5: Commit**

```bash
git add src/nativeMain/kotlin/dev/sriniketh/DecodingCommand.kt
git commit -m "refactor: update DecodingCommand to handle base64 exception instead of null"
```
