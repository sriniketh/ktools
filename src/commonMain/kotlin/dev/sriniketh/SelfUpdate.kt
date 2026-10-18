package dev.sriniketh

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.ByteString
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath

/**
 * Name of the release asset that lists the SHA-256 checksum of every other asset.
 */
private const val CHECKSUMS_ASSET_NAME = "SHA256SUMS"

/**
 * A GitHub release, as returned by the GitHub releases API.
 *
 * @property[tagName] Git tag of the release, e.g. `v0.5.0`.
 * @property[assets] Files attached to the release.
 */
@Serializable
internal data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
    val assets: List<GitHubAsset>,
)

/**
 * A file attached to a GitHub release.
 *
 * @property[name] File name of the asset.
 * @property[downloadUrl] URL the asset can be downloaded from.
 */
@Serializable
internal data class GitHubAsset(
    val name: String,
    @SerialName("browser_download_url") val downloadUrl: String,
)

/**
 * Platforms ktools is released for, each with the name of its release asset.
 */
internal enum class ReleaseTarget(val assetName: String) {
    MACOS_ARM64("macos-arm-ktools.kexe"),
    MACOS_X64("macos-x64-ktools.kexe"),
    LINUX_X64("linux-x64-ktools.kexe"),
    WINDOWS_X64("windows-x64-ktools.exe"),
}

/**
 * A `MAJOR.MINOR.PATCH` release version, ordered numerically.
 */
private data class ReleaseVersion(val major: Int, val minor: Int, val patch: Int) : Comparable<ReleaseVersion> {

    override fun compareTo(other: ReleaseVersion): Int =
        compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch })

    companion object {
        private val pattern = Regex("""^v?(\d{1,9})\.(\d{1,9})\.(\d{1,9})(-.*)?$""")

        /**
         * Parses the release version at the start of a `git describe --tags` string.
         *
         * A dev build such as `v0.5.0-19-g6b1c7a0` parses as `0.5.0`, the release it was built on top of.
         *
         * @param[version] Version string, e.g. `v0.5.0` or `v0.5.0-19-g6b1c7a0`.
         * @return Parsed version, or null if [version] does not start with `vMAJOR.MINOR.PATCH`.
         */
        fun parse(version: String): ReleaseVersion? =
            pattern.matchEntire(version)?.destructured?.let { (major, minor, patch) ->
                ReleaseVersion(major.toInt(), minor.toInt(), patch.toInt())
            }
    }
}

/**
 * Outcome of comparing the running ktools against the latest release.
 */
internal sealed interface UpdatePlan {
    val current: String
    val latest: String

    /**
     * The running ktools is at or after the latest release.
     */
    data class UpToDate(override val current: String, override val latest: String) : UpdatePlan

    /**
     * A newer release exists and can be installed from [binaryUrl], verified against [checksumsUrl].
     */
    data class Available(
        override val current: String,
        override val latest: String,
        val binaryUrl: String,
        val checksumsUrl: String,
    ) : UpdatePlan
}

/**
 * Raised when ktools cannot be updated.
 */
internal class UpdateException(message: String, cause: Throwable? = null) : Exception(message, cause)

private val releaseJson = Json { ignoreUnknownKeys = true }

/**
 * Parses a GitHub release API response.
 *
 * @param[json] Response body of the GitHub release API.
 * @return Parsed release.
 * @throws UpdateException if [json] is not a GitHub release.
 */
@Throws(UpdateException::class)
internal fun parseGitHubRelease(json: String): GitHubRelease =
    try {
        releaseJson.decodeFromString<GitHubRelease>(json)
    } catch (exception: SerializationException) {
        throw UpdateException("Unexpected response from GitHub: ${exception.message}", exception)
    }

/**
 * Decides whether [release] is an update for the running ktools.
 *
 * An unparseable [currentVersion] is treated as older than any release.
 *
 * @param[currentVersion] Version of the running ktools, as produced by `git describe --tags`.
 * @param[release] Latest ktools release.
 * @param[target] Platform the running ktools was built for.
 * @return [UpdatePlan.Available] if [release] is newer than [currentVersion], [UpdatePlan.UpToDate] otherwise.
 * @throws UpdateException if the release tag is not a version, or the release lacks the binary for [target]
 * or the checksums file.
 */
@Throws(UpdateException::class)
internal fun planUpdate(currentVersion: String, release: GitHubRelease, target: ReleaseTarget): UpdatePlan {
    val latest = ReleaseVersion.parse(release.tagName)
        ?: throw UpdateException("Latest release tag ${release.tagName} is not a version")
    val current = ReleaseVersion.parse(currentVersion)
    if (current != null && current >= latest) return UpdatePlan.UpToDate(currentVersion, release.tagName)
    return UpdatePlan.Available(
        current = currentVersion,
        latest = release.tagName,
        binaryUrl = release.assetUrl(target.assetName),
        checksumsUrl = release.assetUrl(CHECKSUMS_ASSET_NAME),
    )
}

private fun GitHubRelease.assetUrl(name: String): String =
    assets.firstOrNull { it.name == name }?.downloadUrl
        ?: throw UpdateException("Release $tagName has no $name asset")

/**
 * Parses a checksums file in `sha256sum` format (`<hex>  <filename>` per line).
 *
 * @param[text] Contents of the checksums file.
 * @return Lowercase hex SHA-256 checksum by file name.
 */
internal fun parseSha256Sums(text: String): Map<String, String> =
    text.lineSequence()
        .map { it.trim().split(Regex("""\s+"""), limit = 2) }
        .filter { it.size == 2 }
        .associate { (hash, name) -> name.removePrefix("*") to hash.lowercase() }

/**
 * Checks that [contents] matches the checksum listed for [assetName] in [sha256Sums].
 *
 * @param[contents] Downloaded asset.
 * @param[sha256Sums] Contents of the release checksums file.
 * @param[assetName] File name of the downloaded asset.
 * @throws UpdateException if [sha256Sums] has no entry for [assetName] or the checksum differs.
 */
@Throws(UpdateException::class)
internal fun verifySha256(contents: ByteString, sha256Sums: String, assetName: String) {
    val expected = parseSha256Sums(sha256Sums)[assetName]
        ?: throw UpdateException("$CHECKSUMS_ASSET_NAME has no entry for $assetName")
    val actual = contents.sha256().hex()
    if (actual != expected) {
        throw UpdateException("Checksum mismatch for $assetName: expected $expected, got $actual")
    }
}

/**
 * Replaces [executable] with [contents], leaving [executable] untouched if anything fails.
 *
 * The new binary is staged next to [executable] and swapped in by rename, because Windows can rename a running
 * executable but not overwrite it. The previous binary is kept as `<executable>.old` until it can be deleted.
 *
 * @param[fileSystem] File system holding [executable].
 * @param[executable] Path of the executable to replace.
 * @param[contents] New executable.
 * @param[makeExecutable] Marks a file as executable.
 * @throws UpdateException if the swap fails and the previous binary cannot be moved back, leaving it at
 * `<executable>.old`.
 * @throws IOException if the new binary cannot be staged or swapped in. [executable] is left as it was.
 */
@Throws(UpdateException::class, IOException::class)
internal fun replaceExecutable(
    fileSystem: FileSystem,
    executable: Path,
    contents: ByteString,
    makeExecutable: (Path) -> Unit,
) {
    val staged = "$executable.new".toPath()
    val previous = "$executable.old".toPath()
    try {
        fileSystem.write(staged) { write(contents) }
        makeExecutable(staged)
        fileSystem.delete(previous, mustExist = false)
        fileSystem.atomicMove(executable, previous)
    } catch (exception: IOException) {
        fileSystem.deleteQuietly(staged)
        throw exception
    }
    try {
        fileSystem.atomicMove(staged, executable)
    } catch (exception: IOException) {
        fileSystem.deleteQuietly(staged)
        fileSystem.restore(previous, executable, exception)
        throw exception
    }
    fileSystem.deleteQuietly(previous)
}

private fun FileSystem.restore(previous: Path, executable: Path, installFailure: IOException) {
    try {
        atomicMove(previous, executable)
    } catch (exception: IOException) {
        throw UpdateException(
            "Could not install the new binary (${installFailure.message}) or put the previous one back. " +
                "The previous binary is now at $previous. Rename it to $executable to restore ktools.",
            installFailure,
        ).apply { addSuppressed(exception) }
    }
}

private fun FileSystem.deleteQuietly(path: Path) {
    try {
        delete(path, mustExist = false)
    } catch (ignored: IOException) {
        // Best effort: Windows cannot delete a running executable, so the next update removes the leftover.
    }
}
