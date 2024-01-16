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
    fun `test unixtime --operation=nowMillis exits with status code 0`() {
        val result = unixTimeCommand.test("--operation=nowMillis")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `test unixtime --operation=nowMillis returns current time in millis`() {
        val result = unixTimeCommand.test("--operation=nowMillis")
        assertEquals("1701331353006", result.stdout.removeNewLines())
    }

    @Test
    fun `test unixtime --operation=nowISO exits with status code 0`() {
        val result = unixTimeCommand.test("--operation=nowISO")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `test unixtime --operation=nowISO returns current time in ISO 8601 format`() {
        val result = unixTimeCommand.test("--operation=nowISO")
        assertEquals("2023-11-30T08:02:33.006Z", result.stdout.removeNewLines())
    }

    @Test
    fun `test unixtime --operation=millisToISO exits with status code 0`() {
        val result = unixTimeCommand.test("--operation=millisToISO --millis=1701331353006")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `test unixtime --operation=millisToISO returns ISO 8601 format string for time in millis`() {
        val result = unixTimeCommand.test("--operation=millisToISO --millis=1701331353006")
        assertEquals("2023-11-30T08:02:33.006Z", result.stdout.removeNewLines())
    }

    @Test
    fun `test unixtime --operation=ISOToMillis exits with status code 0`() {
        val result = unixTimeCommand.test("--operation=ISOToMillis --iso=2023-11-30T08:02:33.006Z")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `test unixtime --operation=ISOToMillis returns time in millis for IS0 8601 format string`() {
        val result = unixTimeCommand.test("--operation=ISOToMillis --iso=2023-11-30T08:02:33.006Z")
        assertEquals("1701331353006", result.stdout.removeNewLines())
    }

    @Test
    fun `test unixtime --operation=ISOToMillis throws exception when ISO string passed is invalid`() {
        val result = unixTimeCommand.test("--operation=ISOToMillis --iso=2023-11-30T08:02:33.006S")
        assertEquals(
            "Illegal argument exception: Parse error at char 23: expected char '-', got 'S",
            result.stdout.removeNewLines()
        )
    }

    private class FakeClock : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1701331353006)
    }
}
