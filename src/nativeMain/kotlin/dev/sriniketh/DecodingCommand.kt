package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.enum

internal class DecodingCommand : CliktCommand(name = "decode") {

    private enum class Format { UTF8, BASE64 }

    private val format by argument(
        name = "format",
        help = "Format to decode from: [utf8 | base64]"
    ).enum<Format> { it.name.lowercase() }
    private val content by option("--string", "-s", help = "Content that needs to be decoded")
        .prompt("Enter string for decoding")
        .check("string must be non-empty") { it.isNotEmpty() }

    override fun help(context: Context): String = "Decode text content"

    override fun run() {
        when (format) {
            Format.UTF8 -> {
                echo("input string: $content")
                try {
                    echo("decoded string: ${decodeFromUTF8Hex(content)}")
                } catch (_: IllegalArgumentException) {
                    echo("Invalid UTF-8 hex input: $content")
                }
            }

            Format.BASE64 -> {
                echo("input string: $content")
                try {
                    echo("decoded string: ${decodeFromBase64(content)}")
                } catch (_: IllegalArgumentException) {
                    echo("Invalid base64 input: $content")
                }
            }
        }
    }
}
