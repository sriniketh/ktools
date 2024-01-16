package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.eagerOption
import dev.sriniketh.helpers.parseFile
import dev.sriniketh.models.LibrariesAndLicenses

internal inline fun <T : CliktCommand> T.aboutOption(): T =
    eagerOption(names = setOf("-a", "--about"), help = "Learn more about ktools and exit") {

        // generated using `./gradlew exportLibraryDefinitions -PaboutLibraries.exportPath=src/nativeMain/resources/ -PaboutLibraries.exportVariant=metadataNativeMain`
        val librariesAndLicensesFilePath = "src/nativeMain/resources/librariesandlicenses.json"
        val librariesAndLicenses = parseFile<LibrariesAndLicenses>(librariesAndLicensesFilePath)

        val stringBuilder = StringBuilder().apply {
            appendLine("ktools is a command line application that provides useful developer tools. It's built as a kotlin multiplatform project.")
            appendLine("Learn more at https://github.com/sriniketh/ktools.")

            appendLine()

            appendLine("Libraries used:")
            librariesAndLicenses.libraries.forEach { library ->
                appendLine("${library.name} : ${library.uniqueId} : ${library.artifactVersion}")
            }

            appendLine()

            appendLine("Licenses:")
            librariesAndLicenses.licenses.forEach { (name, license) ->
                appendLine("$name : ${license.name} : ${license.url}")
            }
        }

        throw PrintMessage(stringBuilder.toString())
    }
