package models

import kotlinx.serialization.Serializable

@Serializable
data class LibrariesAndLicenses(
    val libraries: List<Library>,
    val licenses: Map<String, License>
)

@Serializable
data class Library(
    val name: String?,
    val uniqueId: String,
    val artifactVersion: String?
)

@Serializable
data class License(
    val name: String,
    val url: String?
)
