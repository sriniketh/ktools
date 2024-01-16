package dev.sriniketh

import com.github.ajalt.clikt.testing.test
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HashCommandTest {

    private val fakeFileSystem = FakeFileSystem().apply {
        write("HashingTestFile.txt".toPath()) { writeUtf8("some content") }
    }
    private val hashCommand = HashCommand(fakeFileSystem)

    @Test
    fun `hash returns non 0 status code when no argument is passed for algorithm`() {
        val result = hashCommand.test("--string=blah")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `hash returns non 0 status code when both file and string options are mentioned`() {
        val result = hashCommand.test("md5 --file=HashingTestFile.txt --string=blah")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `hash returns non 0 status code when filepath is empty`() {
        val result = hashCommand.test("md5 --file=")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `hash returns non 0 status code when string is empty`() {
        val result = hashCommand.test("md5 --string=")
        assertNotEquals(0, result.statusCode)
    }

    // MD5

    @Test
    fun `hash md5 exits with status code 0 for string input`() {
        val result = hashCommand.test("md5 --string=blah")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `hash md5 exits with status code 0 for file input`() {
        val result = hashCommand.test("md5 --file=HashingTestFile.txt")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `hash md5 prints exception when file input is invalid`() {
        val result = hashCommand.test("md5 --file=HashingTestFiles.txt")
        assertEquals("IOException: no such file: HashingTestFiles.txt", result.stdout.removeNewLines())
    }

    @Test
    fun `hash md5 returns hash for string input`() {
        val result = hashCommand.test("md5 --string=blah")
        assertEquals("input string: blah\nhash: 6f1ed002ab5595859014ebf0951522d9\n", result.stdout)
    }

    @Test
    fun `hash md5 returns hash for file input`() {
        val result = hashCommand.test("md5 --file=HashingTestFile.txt")
        assertEquals("hash: 9893532233caff98cd083a116b013c0b", result.stdout.removeNewLines())
    }

    // SHA256

    @Test
    fun `hash sha256 exits with status code 0 for string input`() {
        val result = hashCommand.test("sha256 --string=blah")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `hash sha256 exits with status code 0 for file input`() {
        val result = hashCommand.test("sha256 --file=HashingTestFile.txt")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `hash sha256 prints exception when file input is invalid`() {
        val result = hashCommand.test("sha256 --file=HashingTestFiles.txt")
        assertEquals("IOException: no such file: HashingTestFiles.txt", result.stdout.removeNewLines())
    }

    @Test
    fun `hash sha256 returns hash for string input`() {
        val result = hashCommand.test("sha256 --string=blah")
        assertEquals(
            "input string: blah\nhash: 8b7df143d91c716ecfa5fc1730022f6b421b05cedee8fd52b1fc65a96030ad52\n",
            result.stdout
        )
    }

    @Test
    fun `hash sha256 returns hash for file input`() {
        val result = hashCommand.test("sha256 --file=HashingTestFile.txt")
        assertEquals(
            "hash: 290f493c44f5d63d06b374d0a5abd292fae38b92cab2fae5efefe1b0e9347f56",
            result.stdout.removeNewLines()
        )
    }

    // SHA512

    @Test
    fun `hash sha512 exits with status code 0 for string input`() {
        val result = hashCommand.test("sha512 --string=blah")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `hash sha512 exits with status code 0 for file input`() {
        val result = hashCommand.test("sha512 --file=HashingTestFile.txt")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `hash sha512 prints exception when file input is invalid`() {
        val result = hashCommand.test("sha512 --file=HashingTestFiles.txt")
        assertEquals("IOException: no such file: HashingTestFiles.txt", result.stdout.removeNewLines())
    }

    @Test
    fun `hash sha512 returns hash for string input`() {
        val result = hashCommand.test("sha512 --string=blah")
        assertEquals(
            "input string: blah\nhash: 39ca2b1f97c7d1d223dcb2b22cbe20c36f920aeefd201d0bf68ffc08db6d9ac608a0a202fb536d944c9d1f50cf9bd61b5bc84217212f0727a8db8a01c2fa54b7\n",
            result.stdout
        )
    }

    @Test
    fun `hash sha512 returns hash for file input`() {
        val result = hashCommand.test("sha512 --file=HashingTestFile.txt")
        assertEquals(
            "hash: 65c256c639bd6dd483be341831c19a3996954901bb2a07f79593f3e3af5692559bdb124d099c2b92ced7e59b7ed02d3b7f42d50740d999bebd91983db2842762",
            result.stdout.removeNewLines()
        )
    }

    // SHA1

    @Test
    fun `hash sha1 exits with status code 0 for string input`() {
        val result = hashCommand.test("sha1 --string=blah")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `hash sha1 exits with status code 0 for file input`() {
        val result = hashCommand.test("sha1 --file=HashingTestFile.txt")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `hash sha1 prints exception when file input is invalid`() {
        val result = hashCommand.test("sha1 --file=HashingTestFiles.txt")
        assertEquals("IOException: no such file: HashingTestFiles.txt", result.stdout.removeNewLines())
    }

    @Test
    fun `hash sha1 returns hash for string input`() {
        val result = hashCommand.test("sha1 --string=blah")
        assertEquals("input string: blah\nhash: 5bf1fd927dfb8679496a2e6cf00cbe50c1c87145\n", result.stdout)
    }

    @Test
    fun `hash sha1 returns hash for file input`() {
        val result = hashCommand.test("sha1 --file=HashingTestFile.txt")
        assertEquals("hash: 94e66df8cd09d410c62d9e0dc59d3a884e458e05", result.stdout.removeNewLines())
    }
}
