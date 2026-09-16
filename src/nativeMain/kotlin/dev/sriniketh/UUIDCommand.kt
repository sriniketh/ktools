package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import kotlin.time.Clock

private const val UUID_VERSION_4 = 4
private const val UUID_VERSION_7 = 7

internal class UUIDCommand(private val clock: Clock = Clock.System) :
    CliktCommand(name = "uuid", invokeWithoutSubcommand = true) {
    private enum class Case { LOWER, UPPER }

    init {
        subcommands(UUIDInspectCommand())
    }

    private val case by option("-c", "--case", help = "Use upper or lower case. Default is lower.")
        .enum<Case> { it.name.lowercase() }
        .default(Case.LOWER)

    private val version by option("--version", help = "UUID version to generate: [4 | 7]. Default is 4.")
        .int()
        .default(UUID_VERSION_4)
        .check("version must be 4 or 7") { it == UUID_VERSION_4 || it == UUID_VERSION_7 }

    override fun help(context: Context): String = "Create a random UUID"

    override fun run() {
        if (currentContext.invokedSubcommand != null) return

        val uuid = if (version == UUID_VERSION_7) createUuidV7(clock) else createRandomUUID()
        when (case) {
            Case.LOWER -> echo(uuid.lowercase())
            Case.UPPER -> echo(uuid.uppercase())
        }
    }
}
