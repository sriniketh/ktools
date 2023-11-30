plugins {
    kotlin("multiplatform") version "1.9.20-RC2"
}

group = "me.user"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    val platformTargets = listOf(macosX64(), macosArm64())
    configure(platformTargets) {
        binaries {
            executable {
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
        macosMain.dependencies {
            implementation("com.github.ajalt.clikt:clikt:4.2.1")
        }
    }
}
