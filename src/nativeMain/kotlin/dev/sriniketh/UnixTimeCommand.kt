package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import kotlinx.datetime.Clock

internal class UnixTimeCommand(private val clock: Clock = Clock.System) : CliktCommand(name = "unixtime") {

    private sealed class Operation(name: String) : OptionGroup(name)

    private class NowMillis : Operation("Current time in millis")
    private class NowISO : Operation("Current time in ISO 8601 format")
    private class MillisToISO : Operation("Convert time in millis to ISO 8601 format") {
        val timeInMillis by option("--millis").prompt("Enter time in millis")
    }

    private class ISOToMillis : Operation("Convert time in ISO 8601 format to millis") {
        val timeInISO by option("--iso").prompt("Enter time in ISO 8601 format")
    }

    private val operation by option(
        names = arrayOf("-o", "--operation"),
        help = "Choose operation to perform: [nowmillis | nowiso | millistoiso | isotomillis]"
    ).groupChoice(
        "nowmillis" to NowMillis(),
        "nowiso" to NowISO(),
        "millistoiso" to MillisToISO(),
        "isotomillis" to ISOToMillis(),
    )

    override fun help(context: Context): String = "Unix time conversions"

    override fun run() {
        when (val it = operation) {
            is NowMillis -> echo(currentTimeInMillis(clock))
            is NowISO -> echo(currentTimeInIso8601(clock))
            is MillisToISO -> try {
                echo(timeInMillisToIso8601(it.timeInMillis.toLong()))
            } catch (_: NumberFormatException) {
                echo("Number format exception: invalid input ${it.timeInMillis}")
            }

            is ISOToMillis -> try {
                echo(iso8601ToTimeInMillis(it.timeInISO))
            } catch (_: IllegalArgumentException) {
                echo("Illegal argument exception: invalid input ${it.timeInISO}")
            }

            else -> echo("Invalid operation")
        }
    }
}
