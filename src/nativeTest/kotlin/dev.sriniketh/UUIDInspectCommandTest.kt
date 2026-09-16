package dev.sriniketh

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UUIDInspectCommandTest {
    private val uuidCommand = UUIDCommand()

    private val v4Uuid = "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    private val v7Uuid = "018bcfe5-6800-7123-abab-abababababab"
    private val v7TimestampMillis = 1700000000000

    @Test
    fun `test uuid inspect exits with status code 0 for a valid uuid`() {
        val result = uuidCommand.test("inspect $v4Uuid")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `test uuid inspect reports version and variant and no timestamp for a v4 uuid`() {
        val result = uuidCommand.test("inspect $v4Uuid")
        assertContains(result.stdout, "version: 4")
        assertContains(result.stdout, "variant: RFC 4122")
        assertContains(result.stdout, "timestamp: n/a")
    }

    @Test
    fun `test uuid inspect reports human-readable timestamp for a known v7 uuid`() {
        val result = uuidCommand.test("inspect $v7Uuid")
        assertContains(result.stdout, "version: 7")
        assertContains(result.stdout, "timestamp: ${timeInMillisToIso8601(v7TimestampMillis)}")
    }

    @Test
    fun `test uuid inspect exits with non-zero status code and clear error for a malformed uuid`() {
        val result = uuidCommand.test("inspect not-a-uuid")
        assertNotEquals(0, result.statusCode)
        assertContains(result.stderr, "Illegal argument exception")
        assertContains(result.stderr, "not-a-uuid")
    }

    @Test
    fun `test uuid inspect exits with non-zero status code for an incorrectly-sized uuid`() {
        val result = uuidCommand.test("inspect f47ac10b-58cc-4372-a567-0e02b2c3d47")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `test uuid inspect parses uppercase input`() {
        val result = uuidCommand.test("inspect ${v4Uuid.uppercase()}")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "version: 4")
    }

    @Test
    fun `test uuid inspect parses braced input`() {
        val result = uuidCommand.test("inspect {$v4Uuid}")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "version: 4")
    }

    @Test
    fun `test uuid inspect parses urn-prefixed input`() {
        val result = uuidCommand.test("inspect urn:uuid:$v4Uuid")
        assertEquals(0, result.statusCode)
        assertContains(result.stdout, "version: 4")
    }

    @Test
    fun `test uuid inspect --json carries version variant timestamp and raw bytes`() {
        val result = uuidCommand.test("inspect --json $v7Uuid")
        val output = result.stdout.removeNewLines()
        assertContains(output, "\"version\":7")
        assertContains(output, "\"variant\":\"RFC 4122\"")
        assertContains(output, "\"timestamp\":\"${timeInMillisToIso8601(v7TimestampMillis)}\"")
        assertContains(output, "\"rawBytes\":\"${v7Uuid.replace("-", "")}\"")
    }

    @Test
    fun `test uuid inspect does not also print a generated uuid`() {
        val result = uuidCommand.test("inspect $v4Uuid")
        assertEquals(4, result.stdout.trim().lines().size)
    }
}
