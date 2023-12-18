import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.choice

internal class DecodingCommand :
    CliktCommand(name = "decode", help = "Decode text content") {

    private val format by argument(
        name = "format",
        help = "Format to decode from: [utf8 | base64]"
    ).choice(
        "utf8",
        "base64",
        ignoreCase = true
    )
    private val content by option("--text", "-t", help = "Content that needs to be decoded")
        .prompt("Enter text for decoding")
        .check("text must be non-empty") { it.isNotEmpty() }

    override fun run() {
        when (format) {
            "UTF8", "utf8" -> {
                echo("input string: $content")
                echo("decoded string: ${decodeFromUTF8Hex(content)}")
            }

            "BASE64", "base64" -> {
                echo("input string: $content")
                echo("decoded string: ${decodeFromBase64(content)}")
            }
        }
    }
}
