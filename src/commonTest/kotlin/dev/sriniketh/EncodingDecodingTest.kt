package dev.sriniketh

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EncodingDecodingTest {

    @Test
    fun `encodeToUTF8 encodes content to UTF-8 hex`() {
        val result = encodeToUTF8Hex("Foo © bar 𝌆 baz ☃ qux")
        assertEquals("466f6f20c2a92062617220f09d8c862062617a20e2988320717578", result)
    }

    @Test
    fun `decodeFromUTF8 decodes content from UTF-8 hex`() {
        val result = decodeFromUTF8Hex("466f6f20c2a92062617220f09d8c862062617a20e2988320717578")
        assertEquals("Foo © bar 𝌆 baz ☃ qux", result)
    }

    @Test
    fun `encodeToBase64 encodes content to base64`() {
        val result = encodeToBase64("Foo © bar 𝌆 baz ☃ qux")
        assertEquals("Rm9vIMKpIGJhciDwnYyGIGJheiDimIMgcXV4", result)
    }

    @Test
    fun `decodeFromBase64 decodes content from base64`() {
        val result = decodeFromBase64("Rm9vIMKpIGJhciDwnYyGIGJheiDimIMgcXV4")
        assertEquals("Foo © bar 𝌆 baz ☃ qux", result)
    }

    @Test
    fun `decodeFromBase64 throws IllegalArgumentException when provided with invalid base64 input`() {
        assertFailsWith<IllegalArgumentException> {
            decodeFromBase64("4rdHF")
        }
    }
}
