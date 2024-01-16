package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import kotlinx.datetime.Clock

internal class UnixTimeCommand(private val clock: Clock = Clock.System) :
    CliktCommand(name = "unixtime", help = "Unix time conversions") {

    private sealed class Operation(name: String) : OptionGroup(name)

    private class NowMillis : Operation("Current time in millis")
    private class NowISO : Operation("Current time in ISO 8601 format")
    private class MillisToISO : Operation("Convert time in millis to ISO 8601 format") {
        val timeMillis by option("--millis").prompt("Enter time in millis")
    }

    private class ISOToMillis : Operation("Convert time in ISO 8601 format to millis") {
        val timeISO by option("--iso").prompt("Enter time in ISO 8601 format")
    }

    private val operation by option().groupChoice(
        "nowMillis" to NowMillis(),
        "nowISO" to NowISO(),
        "millisToISO" to MillisToISO(),
        "ISOToMillis" to ISOToMillis(),
    )

    override fun run() {
        when (val it = operation) {
            is NowMillis -> echo(currentTimeInMillis(clock))
            is NowISO -> echo(currentTimeInIso8601(clock))
            is MillisToISO -> echo(timeInMillisToIso8601(it.timeMillis.toLong()))
            is ISOToMillis -> try {
                echo(iso8601ToTimeInMillis(it.timeISO))
            } catch (exception: IllegalArgumentException) {
                echo("Illegal argument exception: ${exception.message}")
            }

            else -> echo("Invalid operation")
        }
    }
}
