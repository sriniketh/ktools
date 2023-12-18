import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.encodeUtf8

fun encodeToUTF8Hex(content: String): String = content.encodeUtf8().hex()

fun decodeFromUTF8Hex(encodedContent: String) = encodedContent.decodeHex().utf8()

fun encodeToBase64(content: String): String = content.encodeUtf8().base64()

fun decodeFromBase64(encodedContent: String): String? = encodedContent.decodeBase64()?.utf8()
