package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class UUIDInspectCommand : CliktCommand(name = "inspect") {

    @Serializable
    private data class InspectionJson(
        val version: Int,
        val variant: String,
        val timestamp: String?,
        val rawBytes: String
    )

    private val value by argument(name = "uuid", help = "UUID value to inspect")
    private val asJson by option("--json", help = "Output result as JSON").flag()

    override fun help(context: Context): String =
        "Inspect a UUID and report its version, variant, and (for v1/v6/v7) timestamp"

    override fun run() {
        val inspection = try {
            inspectUuid(value)
        } catch (_: IllegalArgumentException) {
            throw PrintMessage(
                "Illegal argument exception: invalid UUID $value",
                statusCode = 1,
                printError = true
            )
        }

        val timestamp = inspection.timestampMillis?.let { timeInMillisToIso8601(it) }

        if (asJson) {
            echo(
                Json.encodeToString(
                    InspectionJson(
                        version = inspection.version,
                        variant = inspection.variant,
                        timestamp = timestamp,
                        rawBytes = inspection.rawBytesHex
                    )
                )
            )
        } else {
            echo("version: ${inspection.version}")
            echo("variant: ${inspection.variant}")
            echo("timestamp: ${timestamp ?: "n/a"}")
            echo("raw bytes: ${inspection.rawBytesHex}")
        }
    }
}
