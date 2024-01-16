package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.choice
import kotlinx.serialization.SerializationException

internal class PrettyPrintCommand : CliktCommand(name = "prettyprint", help = "Pretty print content") {

    private val contenttype by argument(
        name = "contenttype",
        help = "Pretty print content type: [json | cookie-header]"
    ).choice(
        "json",
        "cookie-header",
        ignoreCase = true
    )
    private val content by option("--string", "-s", help = "Content that needs to be formatted")
        .prompt("Enter string for pretty printing")
        .check("string must be non-empty") { it.isNotEmpty() }

    override fun run() {
        when (contenttype.lowercase()) {
            "json" -> {
                try {
                    echo()
                    echo(prettyPrintJson(content))
                } catch (exception: IllegalArgumentException) {
                    echo("IllegalArgumentException: ${exception.message}")
                } catch (exception: SerializationException) {
                    echo("SerializationException: ${exception.message}")
                }
            }

            "cookie-header" -> {
                echo()
                echo(prettyPrintCookieHeader(content))
            }
        }
    }
}
