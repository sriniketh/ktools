package dev.sriniketh

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import platform.windows.GetLastError
import platform.windows.GetModuleFileNameW
import platform.windows.WCHARVar

private const val MAX_PATH_LENGTH = 32_768

@OptIn(ExperimentalForeignApi::class)
@Throws(IOException::class)
internal actual fun currentExecutablePath(): Path = memScoped {
    val buffer = allocArray<WCHARVar>(MAX_PATH_LENGTH)
    val length = GetModuleFileNameW(null, buffer, MAX_PATH_LENGTH.convert())
    if (length == 0u) throw IOException("Could not locate the ktools executable: error ${GetLastError()}")
    buffer.toKString().toPath()
}

@Throws(IOException::class)
internal actual fun makeExecutable(path: Path) = Unit

internal actual val currentReleaseTarget: ReleaseTarget = ReleaseTarget.WINDOWS_X64
