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

        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.okio.core)
            implementation(libs.kotlinx.serialization)
            implementation(libs.ktor.client)
            implementation(libs.uuid)
        }
        commonTest.dependencies {
            implementation(libs.okio.fakefilesystem)
        }
        nativeMain.dependencies {
            implementation(libs.clikt)
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
