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
 */
fun decodeFromUTF8Hex(encodedContent: String) = encodedContent.decodeHex().utf8()

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
 */
fun decodeFromBase64(encodedContent: String): String? = encodedContent.decodeBase64()?.utf8()
