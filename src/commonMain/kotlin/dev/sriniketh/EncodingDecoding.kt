package dev.sriniketh

import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.encodeUtf8

/**
 * Encodes given content to UTF-8 (hex).
 *
 * @param[content] Content to be encoded.
 * @return Encoded string in UTF-8 (hex) format.
 */
fun encodeToUTF8Hex(content: String): String = content.encodeUtf8().hex()

/**
 * Decodes given UTF-8 (hex) encoded content.
 *
 * @param[encodedContent] Content to be decoded.
 * @return Decoded string.
 * @throws IllegalArgumentException if the content is not valid hex or the bytes are not valid UTF-8.
 */
@Throws(IllegalArgumentException::class)
fun decodeFromUTF8Hex(encodedContent: String): String {
    val bytes = encodedContent.decodeHex().toByteArray()
    try {
        return bytes.decodeToString(throwOnInvalidSequence = true)
    } catch (_: CharacterCodingException) {
        throw IllegalArgumentException("Invalid UTF-8 hex input: $encodedContent")
    }
}

/**
 * Encodes given content to base64.
 *
 * @param[content] Content to be encoded.
 * @return Encoded string in base64 format.
 */
fun encodeToBase64(content: String): String = content.encodeUtf8().base64()

/**
 * Decodes given base64 encoded content.
 *
 * @param[encodedContent] Content to be decoded.
 * @return Decoded string.
 * @throws IllegalArgumentException if the content is not valid base64 or the decoded bytes are not valid UTF-8.
 */
@Throws(IllegalArgumentException::class)
fun decodeFromBase64(encodedContent: String): String {
    val bytes = encodedContent.decodeBase64()?.toByteArray()
        ?: throw IllegalArgumentException("Invalid base64 input: $encodedContent")
    try {
        return bytes.decodeToString(throwOnInvalidSequence = true)
    } catch (_: CharacterCodingException) {
        throw IllegalArgumentException("Invalid base64 input: $encodedContent")
    }
}
