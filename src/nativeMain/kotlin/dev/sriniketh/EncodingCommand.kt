package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.enum

internal class EncodingCommand : CliktCommand(name = "encode") {

    private enum class Format { UTF8, BASE64 }

    private val format by argument(
        name = "format",
        help = "Format to encode to: [utf8 | base64]"
    ).enum<Format> { it.name.lowercase() }
    private val content by option("--string", "-s", help = "Content that needs to be encoded")
        .prompt("Enter string for encoding")
        .check("string must be non-empty") { it.isNotEmpty() }

    override fun help(context: Context): String = "Encode text content"

    override fun run() {
        when (format) {
            Format.UTF8 -> {
                echo("input string: $content")
                echo("encoded string: ${encodeToUTF8Hex(content)}")
            }

            Format.BASE64 -> {
                echo("input string: $content")
                echo("encoded string: ${encodeToBase64(content)}")
            }
        }
    }
}
