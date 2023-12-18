import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.choice

internal class EncodingCommand :
    CliktCommand(name = "encode", help = "Encode text content") {

    private val format by argument(
        name = "format",
        help = "Format to encode to: [utf8 | base64]"
    ).choice(
        "utf8",
        "base64",
        ignoreCase = true
    )
    private val content by option("--text", "-t", help = "Content that needs to be encoded")
        .prompt("Enter text for encoding")
        .check("text must be non-empty") { it.isNotEmpty() }

    override fun run() {
        when (format) {
            "UTF8", "utf8" -> {
                echo("input string: $content")
                echo("encoded string: ${encodeToUTF8Hex(content)}")
            }

            "BASE64", "base64" -> {
                echo("input string: $content")
                echo("encoded string: ${encodeToBase64(content)}")
            }
        }
    }
}
