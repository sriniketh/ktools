import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PrettyPrintCommandTest {

    private val prettyPrintCommand = PrettyPrintCommand()

    @Test
    fun `prettyprint returns non 0 status code when no contenttype is mentioned`() {
        val result = prettyPrintCommand.test("")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `prettyprint returns non 0 status code invalid contenttype is used`() {
        val result = prettyPrintCommand.test("something")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `prettyprint returns non 0 status code when no string is passed`() {
        val result = prettyPrintCommand.test("json")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `prettyprint returns non 0 status code when empty string is passed`() {
        val result = prettyPrintCommand.test("json --string=")
        assertNotEquals(0, result.statusCode)
    }

    @Test
    fun `prettyprint returns 0 status code when valid inputs passed`() {
        val result =
            prettyPrintCommand.test("""json --string="{\"a\":42, \"b\": \"something\", \"c\": {\"d\":43, \"e\": \"this other thing\"}}"""")
        assertEquals(0, result.statusCode)
    }

    // json

    @Test
    fun `prettyprint formats json`() {
        val result =
            prettyPrintCommand.test("""json --string="{\"a\":42, \"b\": \"something\", \"c\": {\"d\":43, \"e\": \"this other thing\"}}"""")
        assertEquals(
            """
                
                {
                    "a": 42,
                    "b": "something",
                    "c": {
                        "d": 43,
                        "e": "this other thing"
                    }
                }
                
            """.trimIndent(),
            result.stdout
        )
    }

    @Test
    fun `prettyprint throws exception for invalid json`() {
        val result =
            prettyPrintCommand.test("""json --string="{\"a\":42, \"b\": \"something\", \"c\": {\"d\":43, \""""")
        assertEquals(
            """
                
                IllegalArgumentException: Unexpected JSON token at offset 42: EOF at path: $['c']
                JSON input: {"a":42, "b": "something", "c": {"d":43, "
                
            """.trimIndent(),
            result.stdout
        )
    }

    // cookie-header

    @Test
    fun `prettyprint formats cookie-header`() {
        val result =
            prettyPrintCommand.test("""cookie-header --string="_gh_sess=iN54V4vle8IYYp7lGgfhpdzrxkNk8vyA3JlUR%2FQc13ceXiGMJadCWU8h3DjYr8W9Z9weu5jbHLsINJw1ozNWyMAjWEGfo62JwoQTWXyFbFCXAgbVzmhnuJjj62WBaGA1nUSZ02GpDyx5hP3pQIB77gTObga3xuxrFSlEVHo%2BwFtxxbosuC8Uv%2Bq%2F45FGzv2NLin56tOx%2FisHITxqFeVqyqZJEf7huR3LKiIJ9WEw%2BpWcGilE6IRFs9o9rGbL%2Fz9CXJIbP4teaaYX6cD5GfGZhw%3D%3D--AiLj3%2BfDqfI%2FU5x6--PSOQUFXsVQbGMZybB87ANw%3D%3D; _octo=GH1.1.844398143.1702713910; logged_in=no; preferred_color_mode=dark; tz=America%2FLos_Angeles"""")
        assertEquals(
            """
                
                {
                    "_gh_sess": "iN54V4vle8IYYp7lGgfhpdzrxkNk8vyA3JlUR%2FQc13ceXiGMJadCWU8h3DjYr8W9Z9weu5jbHLsINJw1ozNWyMAjWEGfo62JwoQTWXyFbFCXAgbVzmhnuJjj62WBaGA1nUSZ02GpDyx5hP3pQIB77gTObga3xuxrFSlEVHo%2BwFtxxbosuC8Uv%2Bq%2F45FGzv2NLin56tOx%2FisHITxqFeVqyqZJEf7huR3LKiIJ9WEw%2BpWcGilE6IRFs9o9rGbL%2Fz9CXJIbP4teaaYX6cD5GfGZhw%3D%3D--AiLj3%2BfDqfI%2FU5x6--PSOQUFXsVQbGMZybB87ANw%3D%3D",
                    "_octo": "GH1.1.844398143.1702713910",
                    "logged_in": "no",
                    "preferred_color_mode": "dark",
                    "tz": "America%2FLos_Angeles"
                }
                
            """.trimIndent(),
            result.stdout
        )
    }
}
