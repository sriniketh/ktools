package dev.sriniketh

import com.github.ajalt.clikt.testing.test
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class UnixTimeCommandTest {

    private val fakeClock: FakeClock = FakeClock()
    private val unixTimeCommand = UnixTimeCommand(clock = fakeClock)

    @Test
    fun `test unixtime --operation=nowmillis exits with status code 0`() {
        val result = unixTimeCommand.test("--operation=nowmillis")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `test unixtime --operation=nowmillis returns current time in millis`() {
        val result = unixTimeCommand.test("--operation=nowmillis")
        assertEquals("1701331353006", result.stdout.removeNewLines())
    }

    @Test
    fun `test unixtime --operation=nowiso exits with status code 0`() {
        val result = unixTimeCommand.test("--operation=nowiso")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `test unixtime --operation=nowiso returns current time in ISO 8601 format`() {
        val result = unixTimeCommand.test("--operation=nowiso")
        assertEquals("2023-11-30T08:02:33.006Z", result.stdout.removeNewLines())
    }

    @Test
    fun `test unixtime --operation=millistoiso exits with status code 0`() {
        val result = unixTimeCommand.test("--operation=millistoiso --millis=1701331353006")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `test unixtime --operation=millistoiso returns ISO 8601 format string for time in millis`() {
        val result = unixTimeCommand.test("--operation=millistoiso --millis=1701331353006")
        assertEquals("2023-11-30T08:02:33.006Z", result.stdout.removeNewLines())
    }

    @Test
    fun `test unixtime --operation=millistoiso throws exception when ISO string passed is invalid`() {
        val result = unixTimeCommand.test("--operation=millistoiso --millis=abcdef")
        assertEquals(
            "Number format exception: invalid input abcdef",
            result.stdout.removeNewLines()
        )
    }

    @Test
    fun `test unixtime --operation=isotomillis exits with status code 0`() {
        val result = unixTimeCommand.test("--operation=isotomillis --iso=2023-11-30T08:02:33.006Z")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `test unixtime --operation=isotomillis returns time in millis for IS0 8601 format string`() {
        val result = unixTimeCommand.test("--operation=isotomillis --iso=2023-11-30T08:02:33.006Z")
        assertEquals("1701331353006", result.stdout.removeNewLines())
    }

    @Test
    fun `test unixtime --operation=isotomillis throws exception when ISO string passed is invalid`() {
        val result = unixTimeCommand.test("--operation=isotomillis --iso=2023-11-30T08:02:33.006S")
        assertEquals(
            "Illegal argument exception: invalid input 2023-11-30T08:02:33.006S",
            result.stdout.removeNewLines()
        )
    }

    private class FakeClock : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1701331353006)
    }
}
