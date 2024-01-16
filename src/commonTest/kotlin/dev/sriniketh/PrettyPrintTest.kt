package dev.sriniketh

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

    @Test
    fun `prettyPrintCookieHeader prints formatted cookie header`() {
        val cookieHeader =
            """_gh_sess=iN54V4vle8IYYp7lGgfhpdzrxkNk8vyA3JlUR%2FQc13ceXiGMJadCWU8h3DjYr8W9Z9weu5jbHLsINJw1ozNWyMAjWEGfo62JwoQTWXyFbFCXAgbVzmhnuJjj62WBaGA1nUSZ02GpDyx5hP3pQIB77gTObga3xuxrFSlEVHo%2BwFtxxbosuC8Uv%2Bq%2F45FGzv2NLin56tOx%2FisHITxqFeVqyqZJEf7huR3LKiIJ9WEw%2BpWcGilE6IRFs9o9rGbL%2Fz9CXJIbP4teaaYX6cD5GfGZhw%3D%3D--AiLj3%2BfDqfI%2FU5x6--PSOQUFXsVQbGMZybB87ANw%3D%3D; _octo=GH1.1.844398143.1702713910; logged_in=no; preferred_color_mode=dark; tz=America%2FLos_Angeles"""
        val expectedPrettyJson = """
            {
                "_gh_sess": "iN54V4vle8IYYp7lGgfhpdzrxkNk8vyA3JlUR%2FQc13ceXiGMJadCWU8h3DjYr8W9Z9weu5jbHLsINJw1ozNWyMAjWEGfo62JwoQTWXyFbFCXAgbVzmhnuJjj62WBaGA1nUSZ02GpDyx5hP3pQIB77gTObga3xuxrFSlEVHo%2BwFtxxbosuC8Uv%2Bq%2F45FGzv2NLin56tOx%2FisHITxqFeVqyqZJEf7huR3LKiIJ9WEw%2BpWcGilE6IRFs9o9rGbL%2Fz9CXJIbP4teaaYX6cD5GfGZhw%3D%3D--AiLj3%2BfDqfI%2FU5x6--PSOQUFXsVQbGMZybB87ANw%3D%3D",
                "_octo": "GH1.1.844398143.1702713910",
                "logged_in": "no",
                "preferred_color_mode": "dark",
                "tz": "America%2FLos_Angeles"
            }
        """.trimIndent()
        val actualPrettyJson = prettyPrintCookieHeader(cookieHeader)
        assertEquals(expectedPrettyJson, actualPrettyJson)
    }
}
