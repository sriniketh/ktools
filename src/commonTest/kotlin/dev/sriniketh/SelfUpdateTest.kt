package dev.sriniketh

import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SelfUpdateTest {

    private val binaryUrl = "https://example.com/v0.6.0/linux-x64-ktools.kexe"
    private val checksumsUrl = "https://example.com/v0.6.0/SHA256SUMS"
    private val release = GitHubRelease(
        tagName = "v0.6.0",
        assets = listOf(
            GitHubAsset("macos-arm-ktools.kexe", "https://example.com/v0.6.0/macos-arm-ktools.kexe"),
            GitHubAsset("linux-x64-ktools.kexe", binaryUrl),
            GitHubAsset("SHA256SUMS", checksumsUrl),
        )
    )

    private fun plan(currentVersion: String, release: GitHubRelease = this.release) =
        planUpdate(currentVersion, release, ReleaseTarget.LINUX_X64)

    @Test
    fun `planUpdate offers the latest release when it is newer`() {
        assertEquals(UpdatePlan.Available("v0.5.0", "v0.6.0", binaryUrl, checksumsUrl), plan("v0.5.0"))
    }

    @Test
    fun `planUpdate compares versions numerically`() {
        assertEquals(UpdatePlan.UpToDate("v0.10.0", "v0.6.0"), plan("v0.10.0"))
    }

    @Test
    fun `planUpdate is up to date when current equals latest`() {
        assertEquals(UpdatePlan.UpToDate("v0.6.0", "v0.6.0"), plan("v0.6.0"))
    }

    @Test
    fun `planUpdate is up to date for a dev build on top of the latest release`() {
        assertEquals(UpdatePlan.UpToDate("v0.6.0-19-g6b1c7a0", "v0.6.0"), plan("v0.6.0-19-g6b1c7a0"))
    }

    @Test
    fun `planUpdate offers the latest release to a dev build on top of an older release`() {
        assertEquals(
            UpdatePlan.Available("v0.5.0-19-g6b1c7a0", "v0.6.0", binaryUrl, checksumsUrl),
            plan("v0.5.0-19-g6b1c7a0")
        )
    }

    @Test
    fun `planUpdate is up to date when current is newer than latest`() {
        assertEquals(UpdatePlan.UpToDate("v1.0.0", "v0.6.0"), plan("v1.0.0"))
    }

    @Test
    fun `planUpdate offers the latest release when current version is unparseable`() {
        assertEquals(UpdatePlan.Available("6b1c7a0", "v0.6.0", binaryUrl, checksumsUrl), plan("6b1c7a0"))
    }

    @Test
    fun `planUpdate throws when the release tag is not a version`() {
        assertFailsWith<UpdateException> { plan("v0.5.0", release.copy(tagName = "nightly")) }
    }

    @Test
    fun `planUpdate throws when the release has no binary for the target`() {
        val withoutBinary = release.copy(assets = release.assets.filter { it.name != "linux-x64-ktools.kexe" })
        assertFailsWith<UpdateException> { plan("v0.5.0", withoutBinary) }
    }

    @Test
    fun `planUpdate throws when the release has no checksums file`() {
        val withoutChecksums = release.copy(assets = release.assets.filter { it.name != "SHA256SUMS" })
        assertFailsWith<UpdateException> { plan("v0.5.0", withoutChecksums) }
    }

    @Test
    fun `parseGitHubRelease reads tag and assets and ignores other fields`() {
        val json = """
            {
              "tag_name": "v0.6.0",
              "name": "v0.6.0",
              "assets": [{ "name": "SHA256SUMS", "size": 10, "browser_download_url": "$checksumsUrl" }]
            }
        """.trimIndent()
        assertEquals(GitHubRelease("v0.6.0", listOf(GitHubAsset("SHA256SUMS", checksumsUrl))), parseGitHubRelease(json))
    }

    @Test
    fun `parseGitHubRelease throws when the response is not a release`() {
        assertFailsWith<UpdateException> { parseGitHubRelease("""{"message": "Not Found"}""") }
    }

    @Test
    fun `parseSha256Sums maps file names to lowercase checksums`() {
        val sums = """
            ABC123  linux-x64-ktools.kexe
            def456 *windows-x64-ktools.exe

        """.trimIndent()
        assertEquals(
            mapOf("linux-x64-ktools.kexe" to "abc123", "windows-x64-ktools.exe" to "def456"),
            parseSha256Sums(sums)
        )
    }

    @Test
    fun `verifySha256 accepts contents matching the listed checksum`() {
        val sums = "290f493c44f5d63d06b374d0a5abd292fae38b92cab2fae5efefe1b0e9347f56  linux-x64-ktools.kexe"
        verifySha256("some content".encodeUtf8(), sums, "linux-x64-ktools.kexe")
    }

    @Test
    fun `verifySha256 throws when the checksum differs`() {
        val sums = "290f493c44f5d63d06b374d0a5abd292fae38b92cab2fae5efefe1b0e9347f56  linux-x64-ktools.kexe"
        assertFailsWith<UpdateException> { verifySha256("tampered".encodeUtf8(), sums, "linux-x64-ktools.kexe") }
    }

    @Test
    fun `verifySha256 throws when the asset is not listed`() {
        val sums = "290f493c44f5d63d06b374d0a5abd292fae38b92cab2fae5efefe1b0e9347f56  macos-arm-ktools.kexe"
        assertFailsWith<UpdateException> { verifySha256("some content".encodeUtf8(), sums, "linux-x64-ktools.kexe") }
    }

    private val installDir = "/usr/local/bin".toPath()
    private val executable = installDir / "ktools"
    private val fakeFileSystem = FakeFileSystem().apply {
        createDirectories(installDir)
        write(executable) { writeUtf8("old") }
    }

    private fun FileSystem.readText(path: Path) = read(path) { readUtf8() }

    @Test
    fun `replaceExecutable installs new contents marked executable`() {
        val markedExecutable = mutableListOf<String>()
        replaceExecutable(fakeFileSystem, executable, "new".encodeUtf8()) {
            markedExecutable += fakeFileSystem.readText(it)
        }
        assertEquals("new", fakeFileSystem.readText(executable))
        assertEquals(listOf("new"), markedExecutable)
        assertEquals(listOf(executable), fakeFileSystem.list(installDir))
    }

    @Test
    fun `replaceExecutable removes the previous binary left by an earlier update`() {
        fakeFileSystem.write(installDir / "ktools.old") { writeUtf8("older") }
        replaceExecutable(fakeFileSystem, executable, "new".encodeUtf8()) {}
        assertEquals("new", fakeFileSystem.readText(executable))
        assertEquals(listOf(executable), fakeFileSystem.list(installDir))
    }

    @Test
    fun `replaceExecutable leaves the executable untouched when the new binary cannot be written`() {
        val readOnly = object : ForwardingFileSystem(fakeFileSystem) {
            override fun sink(file: Path, mustCreate: Boolean): Sink = throw IOException("Permission denied")
        }
        assertFailsWith<IOException> { replaceExecutable(readOnly, executable, "new".encodeUtf8()) {} }
        assertEquals("old", fakeFileSystem.readText(executable))
        assertEquals(listOf(executable), fakeFileSystem.list(installDir))
    }

    @Test
    fun `replaceExecutable restores the executable when the new binary cannot be moved into place`() {
        val failingSwap = object : ForwardingFileSystem(fakeFileSystem) {
            override fun atomicMove(source: Path, target: Path) {
                if (source.name == "ktools.new") throw IOException("Permission denied")
                super.atomicMove(source, target)
            }
        }
        assertFailsWith<IOException> { replaceExecutable(failingSwap, executable, "new".encodeUtf8()) {} }
        assertEquals("old", fakeFileSystem.readText(executable))
        assertEquals(listOf(executable), fakeFileSystem.list(installDir))
    }
}
