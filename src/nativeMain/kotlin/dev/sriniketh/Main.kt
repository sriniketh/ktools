package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import dev.sriniketh.ktools.BuildConfig

/**
 * Entry point for native application.
 */
fun main(args: Array<String>) =
    KTools()
        .versionOption(version = BuildConfig.VERSION, names = setOf("-v", "--version"))
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
