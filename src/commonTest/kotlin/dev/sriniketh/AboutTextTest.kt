package dev.sriniketh

import dev.sriniketh.models.License
import dev.sriniketh.models.Library
import dev.sriniketh.models.LibrariesAndLicenses
import kotlin.test.Test
import kotlin.test.assertTrue

class AboutTextTest {

    @Test
    fun `formatAboutText includes library name id and version`() {
        val librariesAndLicenses = LibrariesAndLicenses(
            libraries = listOf(
                Library(name = "Clikt", uniqueId = "com.github.ajalt.clikt:clikt", artifactVersion = "5.1.0")
            ),
            licenses = mapOf(
                "Apache-2.0" to License(name = "Apache License 2.0", url = "https://spdx.org/licenses/Apache-2.0.html")
            )
        )

        val aboutText = formatAboutText(librariesAndLicenses)

        assertTrue(aboutText.contains("Clikt : com.github.ajalt.clikt:clikt : 5.1.0"))
    }

    @Test
    fun `formatAboutText includes licenses section with license name and url`() {
        val librariesAndLicenses = LibrariesAndLicenses(
            libraries = listOf(
                Library(name = "Okio", uniqueId = "com.squareup.okio:okio", artifactVersion = "3.9.0")
            ),
            licenses = mapOf(
                "Apache-2.0" to License(name = "Apache License 2.0", url = "https://spdx.org/licenses/Apache-2.0.html")
            )
        )

        val aboutText = formatAboutText(librariesAndLicenses)

        assertTrue(aboutText.contains("Licenses:"))
        assertTrue(aboutText.contains("Apache-2.0 : Apache License 2.0 : https://spdx.org/licenses/Apache-2.0.html"))
    }

    @Test
    fun `formatAboutText includes intro description and libraries header`() {
        val librariesAndLicenses = LibrariesAndLicenses(libraries = emptyList(), licenses = emptyMap())

        val aboutText = formatAboutText(librariesAndLicenses)

        assertTrue(aboutText.contains("ktools is a command line application"))
        assertTrue(aboutText.contains("Libraries used:"))
    }
}
