import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UUIDTest {
    private val uuid = UUID()

    @Test
    fun `test uuid exits with status code 0`() {
        val result = uuid.test("")
        assertEquals(0, result.statusCode)
    }

    @Test
    fun `test uuid prints new uuid in lowercase when no arg is provided`() {
        val result = uuid.test("")
        val uuidRegexWithLowercaseChars =
            """^[0-9a-f]{8}\b-[0-9a-f]{4}\b-[0-9a-f]{4}\b-[0-9a-f]{4}\b-[0-9a-f]{12}$""".toRegex()
        val outputWithoutNewLines = result.stdout.removeNewLines()
        assertTrue(uuidRegexWithLowercaseChars.matches(outputWithoutNewLines))
    }

    @Test
    fun `test uuid prints new uuid in upper case when case arg is upper`() {
        val result = uuid.test("-c upper")
        val uuidRegexWithUppercaseChars =
            """^[0-9A-F]{8}\b-[0-9A-F]{4}\b-[0-9A-F]{4}\b-[0-9A-F]{4}\b-[0-9A-F]{12}$""".toRegex()
        val outputWithoutNewLines = result.stdout.removeNewLines()
        assertTrue(uuidRegexWithUppercaseChars.matches(outputWithoutNewLines))
    }

    @Test
    fun `test uuid prints new uuid in lower case when case arg is lower`() {
        val result = uuid.test("-c lower")
        val uuidRegexWithLowercaseChars =
            """^[0-9a-f]{8}\b-[0-9a-f]{4}\b-[0-9a-f]{4}\b-[0-9a-f]{4}\b-[0-9a-f]{12}$""".toRegex()
        val outputWithoutNewLines = result.stdout.removeNewLines()
        assertTrue(uuidRegexWithLowercaseChars.matches(outputWithoutNewLines))
    }

    private fun String.removeNewLines() = replace("""\r\n|\n""".toRegex(), "")
}
