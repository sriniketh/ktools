import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.net.URL

plugins {
    kotlin("multiplatform") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.21"
    distribution
    id("org.jetbrains.dokka") version "1.9.10"
    id("com.mikepenz.aboutlibraries.plugin") version "10.10.0"
}

group = "dev.sriniketh"
version = "0.1.0"

repositories {
    mavenCentral()
}

kotlin {
    macosX64()
    macosArm64()
    linuxX64()
    mingwX64()

    targets.withType<KotlinNativeTarget> {
        binaries {
            executable {
                baseName = "ktools"
                entryPoint = "dev.sriniketh.main"
                runTask?.run {
                    val args = providers.gradleProperty("runArgs")
                    argumentProviders.add(CommandLineArgumentProvider {
                        args.orNull?.split(' ') ?: emptyList()
                    })
                }
            }
        }
    }

    sourceSets {
        val okioVersion = "3.6.0"
        val kotlinxDateTimeVersin = "0.4.1"
        val cliktVersion = "4.2.1"
        val kotlinxSerializationVersion = "1.6.2"
        val ktorVersion = "2.3.7"
        val uuidVersion = "0.0.22"

        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDateTimeVersin")
            implementation("com.squareup.okio:okio:$okioVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            implementation("io.ktor:ktor-client-core:$ktorVersion")
            implementation("app.softwork:kotlinx-uuid-core:$uuidVersion")
        }
        commonTest.dependencies {
            implementation("com.squareup.okio:okio-fakefilesystem:$okioVersion")
        }
        nativeMain.dependencies {
            implementation("com.github.ajalt.clikt:clikt:$cliktVersion")
        }
    }
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets.configureEach {
        displayName.set(name)
        documentedVisibilities.set(
            setOf(Visibility.PUBLIC)
        )
        includes.from(project.files(), "docs/ModuleInfoForDokka.md")
        sourceLink {
            localDirectory.set(file("src/$name/kotlin"))
            remoteUrl.set(
                URL("https://github.com/sriniketh/ktools/tree/main/src/$name/kotlin")
            )
            remoteLineSuffix.set("#L")
        }
    }
}

aboutLibraries {
    registerAndroidTasks = false
    outputFileName = "librariesandlicenses.json"
    prettyPrint = true
}
