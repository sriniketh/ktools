pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.22")
            version("okio", "3.6.0")

            plugin("multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
            plugin("serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
            plugin("dokka", "org.jetbrains.dokka").version("1.9.10")
            plugin("aboutlibraries", "com.mikepenz.aboutlibraries.plugin").version("10.10.0")
            plugin("buildconfig", "com.github.gmazzo.buildconfig").version("5.3.5")

            library("okio-core", "com.squareup.okio", "okio").versionRef("okio")
            library("okio-fakefilesystem", "com.squareup.okio", "okio-fakefilesystem").versionRef("okio")
            library("kotlinx-datetime", "org.jetbrains.kotlinx", "kotlinx-datetime").version("0.4.1")
            library("clikt", "com.github.ajalt.clikt", "clikt").version("4.2.1")
            library("kotlinx-serialization", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version("1.6.2")
            library("ktor-client", "io.ktor", "ktor-client-core").version("2.3.7")
            library("uuid", "app.softwork", "kotlinx-uuid-core").version("0.0.22")
        }
    }
}

rootProject.name = "ktools"
