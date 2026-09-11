package dev.sriniketh.models

import kotlinx.serialization.Serializable

@Serializable
internal data class LibrariesAndLicenses(
    val libraries: List<Library>,
    val licenses: Map<String, License>
)

@Serializable
internal data class Library(
    val name: String?,
    val uniqueId: String,
    val artifactVersion: String?
)

@Serializable
internal data class License(
    val name: String,
    val url: String?
)
