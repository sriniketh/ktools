import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Throws(IllegalArgumentException::class, SerializationException::class)
fun prettyPrintJson(jsonString: String): String {
    val prettyJson = Json { prettyPrint = true }
    val jsonObject = prettyJson.decodeFromString<JsonObject>(jsonString)
    val prettyJsonString = prettyJson.encodeToString(jsonObject)
    return prettyJsonString
}
