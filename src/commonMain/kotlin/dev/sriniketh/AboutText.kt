package dev.sriniketh

import dev.sriniketh.models.LibrariesAndLicenses

/**
 * Formats the "about" text shown by the `--about` option: a short project description
 * followed by the list of third-party libraries and their licenses.
 */
internal fun formatAboutText(librariesAndLicenses: LibrariesAndLicenses): String =
    StringBuilder().apply {
        appendLine(
            "ktools is a command line application that provides useful developer tools. " +
                "It's built as a kotlin multiplatform project."
        )
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
    }.toString()
