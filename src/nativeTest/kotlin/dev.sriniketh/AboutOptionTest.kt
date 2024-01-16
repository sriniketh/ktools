package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.testing.test
import kotlin.test.Test
import kotlin.test.assertEquals

class AboutOptionTest {

    private val testCommand = TestCommand().aboutOption()

    private class TestCommand : CliktCommand() {
        override fun run() = Unit
    }

    @Test
    fun `aboutOption returns 0 status and exits`() {
        val result = testCommand.test("--about")
        assertEquals(0, result.statusCode)
    }
}
