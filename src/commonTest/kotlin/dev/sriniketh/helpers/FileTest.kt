package dev.sriniketh.helpers

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import okio.IOException
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FileTest {

    private val fakeFileSystem = FakeFileSystem().apply {
        write("TestFile.txt".toPath()) {
            writeUtf8(
                """
            {
                "prop1": "some property",
                "prop2": [
                    {
                        "prop3": 123
                    }
                ],
                "prop4": false
            }
            """.trimIndent()
            )
        }
    }

    @Serializable
    private data class TestObject(
        val prop1: String,
        val prop2: List<TestSubObject>
    )

    @Serializable
    private data class TestSubObject(
        val prop3: Int
    )

    @Test
    fun `parseFile parses file contents as given type T`() {
        val result =
            parseFile<TestObject>(filepath = "TestFile.txt", ignoreUnknownKeys = true, fileSystem = fakeFileSystem)
        assertEquals("some property", result.prop1)
        assertEquals(123, result.prop2.first().prop3)
    }

    @Test
    fun `parseFile throws exception when file not found`() {
        assertFailsWith<IOException> {
            parseFile<TestObject>(
                filepath = "TestFileNotFound.txt",
                ignoreUnknownKeys = true,
                fileSystem = fakeFileSystem
            )
        }
    }

    @Test
    fun `parseFile throws exception when there is an unknown property in response and ignoreUnknownKeys is false`() {
        assertFailsWith<SerializationException> {
            parseFile<TestObject>(filepath = "TestFile.txt", ignoreUnknownKeys = false, fileSystem = fakeFileSystem)
        }
    }

    @Test
    fun `parseFile throws exception when content is not a valid instance of type T`() {
        assertFailsWith<IllegalArgumentException> {
            parseFile<Boolean>(filepath = "TestFile.txt", ignoreUnknownKeys = false, fileSystem = fakeFileSystem)
        }
    }
}
