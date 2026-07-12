package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.eagerOption
import dev.sriniketh.ktools.BuildConfig
import dev.sriniketh.models.LibrariesAndLicenses
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

internal fun <T : CliktCommand> T.aboutOption(): T =
    eagerOption(names = setOf("-a", "--about"), help = "Learn more about ktools and exit") {

        val librariesAndLicenses = json.decodeFromString<LibrariesAndLicenses>(BuildConfig.ABOUT_LIBRARIES_JSON)

        throw PrintMessage(formatAboutText(librariesAndLicenses))
    }
