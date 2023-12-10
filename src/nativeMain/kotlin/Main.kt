import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = KTools().subcommands(UUIDCommand(), UnixTimeCommand(), HashCommand()).main(args)

class KTools : CliktCommand() {
    override fun run() = Unit
}
