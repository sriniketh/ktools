package dev.sriniketh

import okio.IOException
import okio.Path

/**
 * Path of the running ktools executable, possibly through a symlink.
 */
@Throws(IOException::class)
internal expect fun currentExecutablePath(): Path

/**
 * Marks [path] as executable by its owner, group and others.
 */
@Throws(IOException::class)
internal expect fun makeExecutable(path: Path)

/**
 * Platform this ktools binary was built for.
 */
internal expect val currentReleaseTarget: ReleaseTarget
