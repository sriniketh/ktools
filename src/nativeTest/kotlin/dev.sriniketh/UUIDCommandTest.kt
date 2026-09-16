package dev.sriniketh

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class UUIDCommandTest {
    private val uuidCommand = UUIDCommand()

    @Test
    fun `test uuid exits with status code 0`() {
        val result = uuidCommand.test("")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `test uuid prints new uuid in lowercase when no arg is provided`() {
        val result = uuidCommand.test("")
        val uuidRegexWithLowercaseChars =
            """^[0-9a-f]{8}\b-[0-9a-f]{4}\b-[0-9a-f]{4}\b-[0-9a-f]{4}\b-[0-9a-f]{12}$""".toRegex()
        val outputWithoutNewLines = result.stdout.removeNewLines()
        assertTrue(uuidRegexWithLowercaseChars.matches(outputWithoutNewLines))
    }

    @Test
    fun `test uuid prints new uuid in upper case when case arg is upper`() {
        val result = uuidCommand.test("-c upper")
        val uuidRegexWithUppercaseChars =
            """^[0-9A-F]{8}\b-[0-9A-F]{4}\b-[0-9A-F]{4}\b-[0-9A-F]{4}\b-[0-9A-F]{12}$""".toRegex()
        val outputWithoutNewLines = result.stdout.removeNewLines()
        assertTrue(uuidRegexWithUppercaseChars.matches(outputWithoutNewLines))
    }

    @Test
    fun `test uuid prints new uuid in lower case when case arg is lower`() {
        val result = uuidCommand.test("-c lower")
        val uuidRegexWithLowercaseChars =
            """^[0-9a-f]{8}\b-[0-9a-f]{4}\b-[0-9a-f]{4}\b-[0-9a-f]{4}\b-[0-9a-f]{12}$""".toRegex()
        val outputWithoutNewLines = result.stdout.removeNewLines()
        assertTrue(uuidRegexWithLowercaseChars.matches(outputWithoutNewLines))
    }

    @Test
    fun `test uuid --version 7 prints a v7 uuid`() {
        val result = uuidCommand.test("--version 7")
        val uuidV7Regex =
            """^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$""".toRegex()
        assertTrue(uuidV7Regex.matches(result.stdout.removeNewLines()))
    }

    @Test
    fun `test uuid --version 4 prints a v4 uuid`() {
        val result = uuidCommand.test("--version 4")
        val uuidV4Regex =
            """^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$""".toRegex()
        assertTrue(uuidV4Regex.matches(result.stdout.removeNewLines()))
    }

    @Test
    fun `test uuid --version with unsupported value exits with non-zero status code`() {
        val result = uuidCommand.test("--version 5")
        assertNotEquals(0, result.statusCode)
    }
}
