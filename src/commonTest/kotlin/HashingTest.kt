import okio.FileNotFoundException
import okio.IOException
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HashingTest {

    private val fakeFileSystem = FakeFileSystem().apply {
        write("HashingTestFile.txt".toPath()) { writeUtf8("some content") }
    }

    @Test
    fun `stringMD5 returns md5 hash of given string`() {
        val md5Hash = stringMD5("some content")
        assertEquals("9893532233caff98cd083a116b013c0b", md5Hash)
    }

    @Test
    fun `stringSHA1 returns sha1 hash of given string`() {
        val sha1Hash = stringSHA1("some content")
        assertEquals("94e66df8cd09d410c62d9e0dc59d3a884e458e05", sha1Hash)
    }

    @Test
    fun `stringSHA256 returns sha256 hash of given string`() {
        val sha256Hash = stringSHA256("some content")
        assertEquals("290f493c44f5d63d06b374d0a5abd292fae38b92cab2fae5efefe1b0e9347f56", sha256Hash)
    }

    @Test
    fun `stringSHA512 returns sha512 hash of given string`() {
        val sha512Hash = stringSHA512("some content")
        assertEquals(
            "65c256c639bd6dd483be341831c19a3996954901bb2a07f79593f3e3af5692559bdb124d099c2b92ced7e59b7ed02d3b7f42d50740d999bebd91983db2842762",
            sha512Hash
        )
    }

    @Test
    fun `fileMD5 returns md5 hash of given file`() {
        val md5Hash = fileMD5("HashingTestFile.txt", fakeFileSystem)
        assertEquals("9893532233caff98cd083a116b013c0b", md5Hash)
    }

    @Test
    fun `fileMD5 throws IOException when file not found`() {
        val exception = assertFailsWith<IOException> {
            fileMD5("FileNotFound.txt", fakeFileSystem)
        }
        assertTrue(exception is FileNotFoundException)
    }

    @Test
    fun `fileSHA1 returns sha1 hash of given file`() {
        val sha1Hash = fileSHA1("HashingTestFile.txt", fakeFileSystem)
        assertEquals("94e66df8cd09d410c62d9e0dc59d3a884e458e05", sha1Hash)
    }

    @Test
    fun `fileSHA1 throws IOException when file not found`() {
        val exception = assertFailsWith<IOException> {
            fileSHA1("FileNotFound.txt", fakeFileSystem)
        }
        assertTrue(exception is FileNotFoundException)
    }

    @Test
    fun `fileSHA256 returns sha256 hash of given file`() {
        val sha256Hash = fileSHA256("HashingTestFile.txt", fakeFileSystem)
        assertEquals("290f493c44f5d63d06b374d0a5abd292fae38b92cab2fae5efefe1b0e9347f56", sha256Hash)
    }

    @Test
    fun `fileSHA256 throws IOException when file not found`() {
        val exception = assertFailsWith<IOException> {
            fileSHA256("FileNotFound.txt", fakeFileSystem)
        }
        assertTrue(exception is FileNotFoundException)
    }

    @Test
    fun `fileSHA512 returns sha512 hash of given file`() {
        val sha512Hash = fileSHA512("HashingTestFile.txt", fakeFileSystem)
        assertEquals(
            "65c256c639bd6dd483be341831c19a3996954901bb2a07f79593f3e3af5692559bdb124d099c2b92ced7e59b7ed02d3b7f42d50740d999bebd91983db2842762",
            sha512Hash
        )
    }

    @Test
    fun `fileSHA512 throws IOException when file not found`() {
        val exception = assertFailsWith<IOException> {
            fileSHA512("FileNotFound.txt", fakeFileSystem)
        }
        assertTrue(exception is FileNotFoundException)
    }
}
