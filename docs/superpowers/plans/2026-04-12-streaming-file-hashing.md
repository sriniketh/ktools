# Streaming File Hashing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace whole-file-in-memory hashing with streaming hashing so large files don't OOM.

**Architecture:** Replace `readByteString()` (loads entire file into RAM) with Okio's `HashingSink` that streams data through in chunks. The public API signatures stay identical — only the internal `getByteString` helper changes to a streaming approach.

**Tech Stack:** Okio (`HashingSink`, `HashingSource`, `Buffer`)

---

### File Structure

- Modify: `src/commonMain/kotlin/dev/sriniketh/Hashing.kt` — replace `getByteString` with per-algorithm streaming functions
- Test: `src/commonTest/kotlin/dev/sriniketh/HashingTest.kt` — existing tests validate correctness (hashes must not change)

---

### Task 1: Replace file hashing internals with streaming approach

**Files:**
- Modify: `src/commonMain/kotlin/dev/sriniketh/Hashing.kt:96-97` (remove `getByteString`)
- Modify: `src/commonMain/kotlin/dev/sriniketh/Hashing.kt:17-20` (`fileMD5`)
- Modify: `src/commonMain/kotlin/dev/sriniketh/Hashing.kt:38-42` (`fileSHA1`)
- Modify: `src/commonMain/kotlin/dev/sriniketh/Hashing.kt:60-64` (`fileSHA256`)
- Modify: `src/commonMain/kotlin/dev/sriniketh/Hashing.kt:82-86` (`fileSHA512`)
- Test: `src/commonTest/kotlin/dev/sriniketh/HashingTest.kt` (existing — no changes needed)

- [x] **Step 1: Run existing tests to establish baseline**

Run: `./gradlew allTests`
Expected: All tests PASS. Note the hash values in `HashingTest.kt` — these must remain identical after refactoring.

- [x] **Step 2: Replace `getByteString` with a streaming `hashFile` helper**

Replace the entire `Hashing.kt` file content with:

```kotlin
package dev.sriniketh

import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.HashingSink
import okio.IOException
import okio.Path.Companion.toPath
import okio.blackholeSink
import okio.buffer

/**
 * Creates MD5 hash of given file.
 *
 * @param[filepath] Path to file that needs to be hashed.
 * @param[fileSystem] File system to use. Defaults to current process host's file system.
 * @return MD5 hash.
 * @throws IOException
 */
@Throws(IOException::class)
fun fileMD5(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String =
    hashFile(filepath, fileSystem, "md5")

/**
 * Creates MD5 hash of given string.
 *
 * @param[content] Content to be hashed.
 * @return MD5 hash.
 */
fun stringMD5(content: String): String = content.encodeUtf8().md5().hex()

/**
 * Creates SHA1 hash of given file.
 *
 * @param[filepath] Path to file that needs to be hashed.
 * @param[fileSystem] File system to use. Defaults to current process host's file system.
 * @return SHA1 hash.
 * @throws IOException
 */
@Throws(IOException::class)
fun fileSHA1(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String =
    hashFile(filepath, fileSystem, "sha1")

/**
 * Creates SHA1 hash of given string.
 *
 * @param[content] Content to be hashed.
 * @return SHA1 hash.
 */
fun stringSHA1(content: String): String = content.encodeUtf8().sha1().hex()

/**
 * Creates SHA256 hash of given file.
 *
 * @param[filepath] Path to file that needs to be hashed.
 * @param[fileSystem] File system to use. Defaults to current process host's file system.
 * @return SHA256 hash.
 * @throws IOException
 */
@Throws(IOException::class)
fun fileSHA256(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String =
    hashFile(filepath, fileSystem, "sha256")

/**
 * Creates SHA256 hash of given string.
 *
 * @param[content] Content to be hashed.
 * @return SHA256 hash.
 */
fun stringSHA256(content: String): String = content.encodeUtf8().sha256().hex()

/**
 * Creates SHA512 hash of given file.
 *
 * @param[filepath] Path to file that needs to be hashed.
 * @param[fileSystem] File system to use. Defaults to current process host's file system.
 * @return SHA512 hash.
 * @throws IOException
 */
@Throws(IOException::class)
fun fileSHA512(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String =
    hashFile(filepath, fileSystem, "sha512")

/**
 * Creates SHA512 hash of given string.
 *
 * @param[content] Content to be hashed.
 * @return SHA512 hash.
 */
fun stringSHA512(content: String): String = content.encodeUtf8().sha512().hex()

private fun hashFile(filepath: String, fileSystem: FileSystem, algorithm: String): String {
    val hashingSink = when (algorithm) {
        "md5" -> HashingSink.md5(blackholeSink())
        "sha1" -> HashingSink.sha1(blackholeSink())
        "sha256" -> HashingSink.sha256(blackholeSink())
        "sha512" -> HashingSink.sha512(blackholeSink())
        else -> throw IllegalArgumentException("Unsupported algorithm: $algorithm")
    }
    fileSystem.source(filepath.toPath()).use { source ->
        hashingSink.buffer().use { bufferedSink ->
            bufferedSink.writeAll(source)
        }
    }
    return hashingSink.hash.hex()
}
```

- [x] **Step 3: Run all tests to confirm hashes are unchanged**

Run: `./gradlew allTests`
Expected: All tests PASS with identical hash values.

- [x] **Step 4: Run detekt**

Run: `./gradlew detektMetadataCommonMain`
Expected: No new violations.

- [x] **Step 5: Commit**

```bash
git add src/commonMain/kotlin/dev/sriniketh/Hashing.kt
git commit -m "refactor: stream file hashing to avoid loading entire file into memory"
```
