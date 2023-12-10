import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "1.9.20-RC2"
    distribution
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
        val okioVersion = "3.6.0"

        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            implementation("com.squareup.okio:okio:$okioVersion")
        }
        commonTest.dependencies {
            implementation("com.squareup.okio:okio-fakefilesystem:$okioVersion")
        }
        nativeMain.dependencies {
            implementation("com.github.ajalt.clikt:clikt:4.2.1")
        }
    }
}
