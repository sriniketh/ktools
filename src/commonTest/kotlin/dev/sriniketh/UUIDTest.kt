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

    private class FixedClock : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1701331353006)
    }
}
