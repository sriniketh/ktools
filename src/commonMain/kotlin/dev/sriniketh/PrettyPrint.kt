package dev.sriniketh

import io.ktor.http.parseClientCookiesHeader
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Pretty prints given json string.
 *
 * @param[jsonString] JSON string that needs to be formatted.
 * @return Formatted json string.
 * @throws IllegalArgumentException
 * @throws SerializationException
 */
@Throws(IllegalArgumentException::class, SerializationException::class)
fun prettyPrintJson(jsonString: String): String {
    val prettyJson = Json { prettyPrint = true }
    val jsonElement = prettyJson.decodeFromString<JsonElement>(jsonString)
    val prettyJsonString = prettyJson.encodeToString(jsonElement)
    return prettyJsonString
}

/**
 * Pretty prints given Cookie header value.
 *
 * @param[cookieHeader] Cookie header that needs to be formatted.
 * @return Formatted Cookie header.
 */
fun prettyPrintCookieHeader(cookieHeader: String): String {
    val cookiesMap = parseClientCookiesHeader(cookieHeader)
    val prettyJson = Json { prettyPrint = true }
    val prettyJsonString = prettyJson.encodeToString(cookiesMap)
    return prettyJsonString
}
