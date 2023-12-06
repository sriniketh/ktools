import kotlin.test.Test
import kotlin.test.assertTrue

class UUIDTest {

    @Test
    fun `test createRandomUUID creates new random UUID`() {
        val uuid = createRandomUUID()
        val uuidRegex =
            """^[0-9A-F]{8}\b-[0-9A-F]{4}\b-[0-9A-F]{4}\b-[0-9A-F]{4}\b-[0-9A-F]{12}$""".toRegex()
        assertTrue(uuidRegex.matches(uuid))
    }
}
