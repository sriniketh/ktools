pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "2.1.10")
            version("okio", "3.10.2")

            plugin("gradle-versions", "com.github.ben-manes.versions").version("0.52.0")

            plugin("multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
            plugin("serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
            plugin("dokka", "org.jetbrains.dokka").version("2.0.0")
            plugin("aboutlibraries", "com.mikepenz.aboutlibraries.plugin").version("11.6.3")
            plugin("buildconfig", "com.github.gmazzo.buildconfig").version("5.5.1")

            library("okio-core", "com.squareup.okio", "okio").versionRef("okio")
            library("okio-fakefilesystem", "com.squareup.okio", "okio-fakefilesystem").versionRef("okio")
            library("kotlinx-datetime", "org.jetbrains.kotlinx", "kotlinx-datetime").version("0.6.2")
            library("clikt", "com.github.ajalt.clikt", "clikt").version("4.4.0")
            library("kotlinx-serialization", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version("1.8.0")
            library("ktor-client", "io.ktor", "ktor-client-core").version("3.1.1")
        }
    }
}

rootProject.name = "ktools"
