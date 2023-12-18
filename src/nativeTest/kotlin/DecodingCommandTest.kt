import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DecodingCommandTest {

    private val decodingCommand = DecodingCommand()

    @Test
    fun `decode returns non 0 status code when no format is mentioned`() {
        val result = decodingCommand.test("")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `decode returns non 0 status code invalid format is used`() {
        val result = decodingCommand.test("md5")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `decode returns non 0 status code when no string is passed`() {
        val result = decodingCommand.test("utf8")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `decode returns non 0 status code when empty string is passed`() {
        val result = decodingCommand.test("utf8 --string=")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `decode returns 0 status code when valid inputs passed`() {
        val result = decodingCommand.test("""utf8 --string="466f6f20c2a92062617220f09d8c862062617a20e2988320717578"""")
        assertEquals(0, result.statusCode)
    }

    // utf-8

    @Test
    fun `decode utf8 decodes content for valid input`() {
        val result = decodingCommand.test("""utf8 --string="466f6f20c2a92062617220f09d8c862062617a20e2988320717578"""")
        assertEquals(
            "input string: 466f6f20c2a92062617220f09d8c862062617a20e2988320717578\ndecoded string: Foo © bar 𝌆 baz ☃ qux\n",
            result.stdout
        )
    }

    // base64

    @Test
    fun `decode base64 decodes content for valid input`() {
        val result = decodingCommand.test("""base64 --string="Rm9vIMKpIGJhciDwnYyGIGJheiDimIMgcXV4"""")
        assertEquals(
            "input string: Rm9vIMKpIGJhciDwnYyGIGJheiDimIMgcXV4\ndecoded string: Foo © bar 𝌆 baz ☃ qux\n",
            result.stdout
        )
    }
}
