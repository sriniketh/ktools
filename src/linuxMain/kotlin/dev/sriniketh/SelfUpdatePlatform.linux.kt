package dev.sriniketh

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.toKString
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import platform.posix.chmod
import platform.posix.errno
import platform.posix.strerror

private const val EXECUTABLE_MODE = 0b111_101_101

@Throws(IOException::class)
internal actual fun currentExecutablePath(): Path = "/proc/self/exe".toPath()

@OptIn(ExperimentalForeignApi::class)
@Throws(IOException::class)
internal actual fun makeExecutable(path: Path) {
    if (chmod(path.toString(), EXECUTABLE_MODE.convert()) != 0) {
        throw IOException("Could not make $path executable: ${strerror(errno)?.toKString()}")
    }
}

internal actual val currentReleaseTarget: ReleaseTarget = ReleaseTarget.LINUX_X64
