import kotlin.test.Test
import kotlin.test.assertEquals

class PrettyPrintTest {

    @Test
    fun `prettyPrintJson prints formatted json`() {
        val inputJson = """{"a":42, "b": "something", "c": {"d":43, "e": "this other thing"}}"""
        val expectedPrettyJson = """
            {
                "a": 42,
                "b": "something",
                "c": {
                    "d": 43,
                    "e": "this other thing"
                }
            }
        """.trimIndent()
        val actualPrettyJson = prettyPrintJson(inputJson)
        assertEquals(expectedPrettyJson, actualPrettyJson)
    }
}
