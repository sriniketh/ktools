package dev.sriniketh

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnixTimeTest {

    private val fakeClock = FakeClock()

    @Test
    fun `currentTimeInMillis returns current time in millis`() {
        val millis = currentTimeInMillis(fakeClock)
        assertEquals(1701331353006, millis)
    }

    @Test
    fun `currentTimeInIso8601 returns current time in iso 8601 format`() {
        val iso8601 = currentTimeInIso8601(fakeClock)
        assertEquals("2023-11-30T08:02:33.006Z", iso8601)
    }

    @Test
    fun `timeInMillisToIso8601 returns given time in millis in iso 8601 format`() {
        val iso8601 = timeInMillisToIso8601(1701331353006)
        assertEquals("2023-11-30T08:02:33.006Z", iso8601)
    }

    @Test
    fun `iso8601ToTimeInMillis returns given time in iso 8601 format in millis`() {
        val millis = iso8601ToTimeInMillis("2023-11-30T08:02:33.006Z")
        assertEquals(1701331353006, millis)
    }

    @Test
    fun `iso8601ToTimeInMillis throws exception when time is not in iso 8601 format`() {
        assertFailsWith<IllegalArgumentException>() {
            iso8601ToTimeInMillis("some thing")
        }
    }

    private class FakeClock : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1701331353006)
    }
}
