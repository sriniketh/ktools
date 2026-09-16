package dev.sriniketh

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class UUIDTest {

    private companion object {
        private const val SAME_MILLISECOND_SAMPLE_SIZE = 10
    }

    // createUuidV7's monotonic counter is shared state across the whole test binary, so a value
    // can't be picked in advance without knowing what other tests already used. Feeding it an
    // old clock value forces its "not newer than last time" guard to report back exactly what
    // it's currently holding; adding 1 to that is then guaranteed to land on a fresh millisecond.
    private fun nextUnusedV7Millis(): Long = inspectUuid(createUuidV7(FixedClock(0))).timestampMillis!! + 1

    @Test
    fun `test createRandomUUID creates new random UUID`() {
        val uuid = createRandomUUID()
        val uuidRegex =
            """^[0-9a-f]{8}\b-[0-9a-f]{4}\b-[0-9a-f]{4}\b-[0-9a-f]{4}\b-[0-9a-f]{12}$""".toRegex()
        assertTrue(uuidRegex.matches(uuid))
    }

    @Test
    fun `test createRandomUUID does not return same ID upon successive calls to function`() {
        val uuid1 = createRandomUUID()
        val uuid2 = createRandomUUID()
        assertNotEquals(uuid1, uuid2)
    }

    @Test
    fun `test createUuidV7 has version nibble 7 and RFC 4122 variant bits`() {
        val uuid = createUuidV7()
        val uuidV7Regex =
            """^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$""".toRegex()
        assertTrue(uuidV7Regex.matches(uuid))
    }

    @Test
    fun `test createUuidV7 generates UUIDs that sort lexicographically in creation order`() {
        val fixedClock = FixedClock()
        val uuid1 = createUuidV7(fixedClock)
        val uuid2 = createUuidV7(fixedClock)
        assertTrue(uuid1 < uuid2)
    }

    @Test
    fun `test createUuidV7 generates a strictly sorted and unique sequence within the same millisecond`() {
        val fixedClock = FixedClock()
        val uuids = List(SAME_MILLISECOND_SAMPLE_SIZE) { createUuidV7(fixedClock) }
        assertEquals(uuids, uuids.sorted())
        assertEquals(uuids.size, uuids.toSet().size)
    }

    @Test
    fun `test createUuidV7 sorts a later timestamp after an earlier one`() {
        val uuid1 = createUuidV7(FixedClock(nextUnusedV7Millis()))
        val uuid2 = createUuidV7(FixedClock(nextUnusedV7Millis()))
        assertTrue(uuid1 < uuid2)
    }

    @Test
    fun `test createUuidV7 embeds the clock's timestamp and RFC 9562 fields as inspectUuid decodes them`() {
        val timestampMillis = nextUnusedV7Millis()
        val uuid = createUuidV7(FixedClock(timestampMillis))
        val inspection = inspectUuid(uuid)
        assertEquals(7, inspection.version)
        assertEquals("RFC 4122", inspection.variant)
        assertEquals(timestampMillis, inspection.timestampMillis)
    }

    @Test
    fun `test inspectUuid on a known v7 returns the expected millisecond timestamp`() {
        val inspection = inspectUuid("018bcfe5-6800-7123-abab-abababababab")
        assertEquals(7, inspection.version)
        assertEquals("RFC 4122", inspection.variant)
        assertEquals(1700000000000, inspection.timestampMillis)
    }

    @Test
    fun `test inspectUuid on a known v1 returns the expected millisecond timestamp`() {
        val inspection = inspectUuid("04afc000-833b-11ee-81ab-0123456789ab")
        assertEquals(1, inspection.version)
        assertEquals("RFC 4122", inspection.variant)
        assertEquals(1700000000000, inspection.timestampMillis)
    }

    @Test
    fun `test inspectUuid on a known v6 returns the expected millisecond timestamp`() {
        val inspection = inspectUuid("1ee833b0-4afc-6000-81ab-0123456789ab")
        assertEquals(6, inspection.version)
        assertEquals("RFC 4122", inspection.variant)
        assertEquals(1700000000000, inspection.timestampMillis)
    }

    @Test
    fun `test inspectUuid on a v4 reports version 4 and no timestamp`() {
        val inspection = inspectUuid("f47ac10b-58cc-4372-a567-0e02b2c3d479")
        assertEquals(4, inspection.version)
        assertEquals("RFC 4122", inspection.variant)
        assertNull(inspection.timestampMillis)
    }

    @Test
    fun `test inspectUuid throws for a malformed string`() {
        assertFailsWith<IllegalArgumentException> {
            inspectUuid("not-a-uuid")
        }
    }

    @Test
    fun `test inspectUuid throws for an incorrectly-sized string`() {
        assertFailsWith<IllegalArgumentException> {
            inspectUuid("f47ac10b-58cc-4372-a567-0e02b2c3d47")
        }
    }

    @Test
    fun `test inspectUuid parses uppercase input`() {
        val inspection = inspectUuid("F47AC10B-58CC-4372-A567-0E02B2C3D479")
        assertEquals(4, inspection.version)
    }

    @Test
    fun `test inspectUuid parses lowercase input`() {
        val inspection = inspectUuid("f47ac10b-58cc-4372-a567-0e02b2c3d479")
        assertEquals(4, inspection.version)
    }

    @Test
    fun `test inspectUuid parses braced input`() {
        val inspection = inspectUuid("{f47ac10b-58cc-4372-a567-0e02b2c3d479}")
        assertEquals(4, inspection.version)
    }

    @Test
    fun `test inspectUuid parses urn-prefixed input`() {
        val inspection = inspectUuid("urn:uuid:f47ac10b-58cc-4372-a567-0e02b2c3d479")
        assertEquals(4, inspection.version)
    }

    @Test
    fun `test inspectUuid returns raw bytes as hex`() {
        val inspection = inspectUuid("f47ac10b-58cc-4372-a567-0e02b2c3d479")
        assertEquals("f47ac10b58cc4372a5670e02b2c3d479", inspection.rawBytesHex)
    }

    private class FixedClock(private val epochMillis: Long = 1701331353006) : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(epochMillis)
    }
}
