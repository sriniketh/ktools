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
            }
        }
    }
}
