import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.ByteArrayOutputStream
import java.net.URL

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    distribution
    alias(libs.plugins.dokka)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.buildconfig)
}

group = "dev.sriniketh"
version = getVersionNameFromGitTags()

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
        outputDirectory.set(file("${rootDir}/docs/dokka/"))
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

buildConfig {
    documentation.set("Generated by BuildConfig plugin")
    buildConfigField("VERSION", version.toString())
}

fun getVersionNameFromGitTags(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "describe", "--tags")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}
