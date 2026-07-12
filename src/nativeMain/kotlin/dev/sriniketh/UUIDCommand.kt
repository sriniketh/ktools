package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum

internal class UUIDCommand : CliktCommand(name = "uuid") {
    private enum class Case { LOWER, UPPER }

    private val case by option("-c", "--case", help = "Use upper or lower case. Default is lower.")
        .enum<Case> { it.name.lowercase() }
        .default(Case.LOWER)

    override fun help(context: Context): String = "Create a random UUID"

    override fun run() {
        val uuid = createRandomUUID()
        when (case) {
            Case.LOWER -> echo(uuid.lowercase())
            Case.UPPER -> echo(uuid.uppercase())
        }
    }
}
