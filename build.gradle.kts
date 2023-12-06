import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "1.9.20-RC2"
}

group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    macosX64()
    macosArm64()

    targets.withType<KotlinNativeTarget> {
        binaries {
            executable {
                baseName = "ktools"
                entryPoint = "main"
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
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
        }
        nativeMain.dependencies {
            implementation("com.github.ajalt.clikt:clikt:4.2.1")
        }
    }
}
