package dev.sriniketh

import com.github.ajalt.clikt.testing.test
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.io.IOException as NetworkIOException

class UpdateCommandTest {

    private val latestReleaseUrl = "https://api.github.com/repos/sriniketh/ktools/releases/latest"
    private val binaryUrl = "https://github.com/sriniketh/ktools/releases/download/v0.6.0/linux-x64-ktools.kexe"
    private val checksumsUrl = "https://github.com/sriniketh/ktools/releases/download/v0.6.0/SHA256SUMS"
    private val releaseJson = """
        {
          "tag_name": "v0.6.0",
          "assets": [
            { "name": "linux-x64-ktools.kexe", "browser_download_url": "$binaryUrl" },
            { "name": "SHA256SUMS", "browser_download_url": "$checksumsUrl" }
          ]
        }
    """.trimIndent()
    private val newBinary = "new binary"

    private var releaseStatus = HttpStatusCode.OK
    private var sha256Sums = "${newBinary.encodeUtf8().sha256().hex()}  linux-x64-ktools.kexe"
    private val engine = MockEngine { request ->
        when (request.url.toString()) {
            latestReleaseUrl -> respond(releaseJson, releaseStatus)
            checksumsUrl -> respond(sha256Sums)
            binaryUrl -> respond(newBinary.encodeUtf8().toByteArray())
            else -> respondError(HttpStatusCode.NotFound)
        }
    }

    private val installDir = "/usr/local/bin".toPath()
    private val executable = installDir / "ktools"
    private val fakeFileSystem = FakeFileSystem().apply {
        createDirectories(installDir)
        write(executable) { writeUtf8("old binary") }
    }

    private fun updateCommand(
        currentVersion: String = "v0.5.0",
        httpClient: () -> HttpClient = { HttpClient(engine) },
        fileSystem: FileSystem = fakeFileSystem,
    ) = UpdateCommand(
        httpClient = httpClient,
        fileSystem = fileSystem,
        currentVersion = currentVersion,
        executablePath = { executable },
        makeExecutable = {},
        target = ReleaseTarget.LINUX_X64,
    )

    private fun installedBinary() = fakeFileSystem.read(executable) { readUtf8() }

    @Test
    fun `update reports up to date and leaves the executable untouched when on the latest release`() {
        val result = updateCommand(currentVersion = "v0.6.0").test("")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "ktools v0.6.0 is already up to date")
        assertEquals("old binary", installedBinary())
    }

    @Test
    fun `update replaces the executable with the latest release`() {
        val result = updateCommand().test("")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "Updated ktools v0.5.0 -> v0.6.0")
        assertEquals(newBinary, installedBinary())
    }

    @Test
    fun `update --check reports the available update without downloading it`() {
        val result = updateCommand().test("--check")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "v0.5.0 -> v0.6.0")
        assertEquals("old binary", installedBinary())
        assertTrue(engine.requestHistory.none { it.url.toString() == binaryUrl })
    }

    @Test
    fun `update --check reports up to date when on the latest release`() {
        val result = updateCommand(currentVersion = "v0.6.0").test("--check")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "ktools v0.6.0 is already up to date")
    }

    @Test
    fun `update fails and leaves the executable untouched when the checksum does not match`() {
        sha256Sums = "${"tampered".encodeUtf8().sha256().hex()}  linux-x64-ktools.kexe"
        val result = updateCommand().test("")
        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "Checksum mismatch")
        assertEquals("old binary", installedBinary())
    }

    @Test
    fun `update fails when the release API returns not found`() {
        releaseStatus = HttpStatusCode.NotFound
        val result = updateCommand().test("")
        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "404")
        assertEquals("old binary", installedBinary())
    }

    @Test
    fun `update fails when the release API returns a server error`() {
        releaseStatus = HttpStatusCode.InternalServerError
        val result = updateCommand().test("--check")
        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "500")
    }

    @Test
    fun `update fails when GitHub cannot be reached`() {
        val offline = MockEngine { throw NetworkIOException("Network is unreachable") }
        val result = updateCommand(httpClient = { HttpClient(offline) }).test("")
        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "Network is unreachable")
        assertEquals("old binary", installedBinary())
    }

    @Test
    fun `update fails naming the install directory and leaves the executable untouched when it is not writable`() {
        val readOnly = object : ForwardingFileSystem(fakeFileSystem) {
            override fun sink(file: Path, mustCreate: Boolean): Sink = throw IOException("Permission denied")
        }
        val result = updateCommand(fileSystem = readOnly).test("")
        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "Permission denied")
        assertContains(result.stderr, installDir.toString())
        assertEquals("old binary", installedBinary())
        assertEquals(listOf(executable), fakeFileSystem.list(installDir))
    }

    @Test
    fun `update explains a locked previous binary and leaves the executable untouched`() {
        val previous = installDir / "ktools.old"
        fakeFileSystem.write(previous) { writeUtf8("older binary") }
        val locked = object : ForwardingFileSystem(fakeFileSystem) {
            override fun delete(path: Path, mustExist: Boolean) {
                if (path == previous) throw IOException("Access is denied")
                super.delete(path, mustExist)
            }
        }
        val result = updateCommand(fileSystem = locked).test("")
        assertEquals(1, result.statusCode)
        assertContains(result.stderr, "Could not remove $previous")
        assertContains(result.stderr, "Close any ktools still running from it")
        assertFalse(result.stderr.contains("Re-run with permission"))
        assertEquals("old binary", installedBinary())
        assertEquals(listOf(executable, previous), fakeFileSystem.list(installDir))
    }
}
