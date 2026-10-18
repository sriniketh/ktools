package dev.sriniketh

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.toKString
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSBundle
import platform.posix.chmod
import platform.posix.errno
import platform.posix.strerror
import kotlin.experimental.ExperimentalNativeApi

private const val EXECUTABLE_MODE = 0b111_101_101

@Throws(IOException::class)
internal actual fun currentExecutablePath(): Path =
    NSBundle.mainBundle.executablePath?.toPath() ?: throw IOException("Could not locate the ktools executable")

@OptIn(ExperimentalForeignApi::class)
@Throws(IOException::class)
internal actual fun makeExecutable(path: Path) {
    if (chmod(path.toString(), EXECUTABLE_MODE.convert()) != 0) {
        throw IOException("Could not make $path executable: ${strerror(errno)?.toKString()}")
    }
}

@OptIn(ExperimentalNativeApi::class)
internal actual val currentReleaseTarget: ReleaseTarget =
    if (Platform.cpuArchitecture == CpuArchitecture.ARM64) ReleaseTarget.MACOS_ARM64 else ReleaseTarget.MACOS_X64
