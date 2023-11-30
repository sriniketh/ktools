import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import platform.Foundation.NSUUID

actual fun createRandomUUID(): String = NSUUID().UUIDString

internal class UUID : CliktCommand(name = "uuid", help = "Create a random UUID") {
    private enum class Case { lower, upper }

    private val case by option("-c", "--case", help = "Use upper or lower case. Default is lower.")
        .enum<Case>()
        .default(Case.lower)

    override fun run() {
        val uuid = createRandomUUID()
        when (case) {
            Case.lower -> echo(uuid.lowercase())
            Case.upper -> echo(uuid)
        }
    }
}
