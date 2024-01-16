package dev.sriniketh

import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class EncodingCommandTest {

    private val encodingCommand = EncodingCommand()

    @Test
    fun `encode returns non 0 status code when no format is mentioned`() {
        val result = encodingCommand.test("")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `encode returns non 0 status code invalid format is used`() {
        val result = encodingCommand.test("md5")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `encode returns non 0 status code when no string is passed`() {
        val result = encodingCommand.test("utf8")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `encode returns non 0 status code when empty string is passed`() {
        val result = encodingCommand.test("utf8 --string=")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `encode returns 0 status code when valid inputs passed`() {
        val result = encodingCommand.test("""utf8 --string="Foo © bar 𝌆 baz ☃ qux"""")
        assertEquals(0, result.statusCode)
    }

    // utf-8

    @Test
    fun `encode utf8 encodes content for valid input`() {
        val result = encodingCommand.test("""utf8 --string="Foo © bar 𝌆 baz ☃ qux"""")
        assertEquals(
            "input string: Foo © bar 𝌆 baz ☃ qux\nencoded string: 466f6f20c2a92062617220f09d8c862062617a20e2988320717578\n",
            result.stdout
        )
    }

    // base64

    @Test
    fun `encode base64 encodes content for valid input`() {
        val result = encodingCommand.test("""base64 --string="Foo © bar 𝌆 baz ☃ qux"""")
        assertEquals(
            "input string: Foo © bar 𝌆 baz ☃ qux\nencoded string: Rm9vIMKpIGJhciDwnYyGIGJheiDimIMgcXV4\n",
            result.stdout
        )
    }
}
