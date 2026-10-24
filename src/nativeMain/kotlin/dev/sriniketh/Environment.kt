package dev.sriniketh

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import platform.posix.getenv

/**
 * Value of the environment variable [name], or null when it is unset or empty.
 */
@OptIn(ExperimentalForeignApi::class)
internal fun environmentVariable(name: String): String? = getenv(name)?.toKString()?.takeIf { it.isNotEmpty() }

/**
 * The user's home directory: `HOME`, or `USERPROFILE` on Windows.
 */
@Throws(IOException::class)
internal fun homeDirectory(): Path =
    (environmentVariable("HOME") ?: environmentVariable("USERPROFILE"))?.toPath()
        ?: throw IOException("Could not find your home directory: set HOME")
