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

            plugin("multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
            plugin("serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
            plugin("dokka", "org.jetbrains.dokka").version("1.9.10")
            plugin("aboutlibraries", "com.mikepenz.aboutlibraries.plugin").version("10.10.0")
            plugin("buildconfig", "com.github.gmazzo.buildconfig").version("5.3.5")
        }
    }
}

rootProject.name = "ktools"
