package dev.sriniketh

import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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
}
