import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

/**
 * Entry point for native application.
 */
fun main(args: Array<String>) =
    KTools()
        .aboutOption()
        .subcommands(
            UUIDCommand(),
            UnixTimeCommand(),
            HashCommand(),
            EncodingCommand(),
            DecodingCommand(),
            PrettyPrintCommand()
        )
        .main(args)

internal class KTools : CliktCommand() {
    override fun run() = Unit
}
