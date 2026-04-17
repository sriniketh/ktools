package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.check
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import okio.FileSystem
import okio.IOException

internal class HashCommand(private val fileSystem: FileSystem = FileSystem.SYSTEM) : CliktCommand(name = "hash") {

    private sealed interface Content
    private class FileContent(val path: String) : Content
    private class StringContent(val text: String) : Content

    private val algorithm by argument(
        name = "algorithm",
        help = "Algorithm to use for hashing: [MD5 | SHA1 | SHA256 | SHA512]"
    ).choice(
        "MD5",
        "SHA1",
        "SHA256",
        "SHA512",
        ignoreCase = true
    )
    private val content: Content by mutuallyExclusiveOptions(
        option("--file", "-f", help = "File to get hash value for (provide filepath)").convert { FileContent(it) }
            .check("filepath must be non-empty") { it.path.isNotEmpty() },
        option("--string", "-s", help = "String to get hash value for").convert { StringContent(it) }
            .check("string must be non-empty") { it.text.isNotEmpty() }
    ).single().required()

    private fun printHashForFileOrExceptionIfFailure(block: () -> String) =
        try {
            echo("hash: ${block.invoke()}")
        } catch (exception: IOException) {
            echo("IOException: ${exception.message}")
        }

    override fun help(context: Context): String = "Get hash value for given file or string"

    override fun run() {
        val (fileHashFn, stringHashFn) = when (algorithm.lowercase()) {
            "md5" -> ::fileMD5 to ::stringMD5
            "sha1" -> ::fileSHA1 to ::stringSHA1
            "sha256" -> ::fileSHA256 to ::stringSHA256
            "sha512" -> ::fileSHA512 to ::stringSHA512
            else -> {
                echo("Unsupported option: $algorithm")
                return
            }
        }

        when (val it = content) {
            is FileContent -> printHashForFileOrExceptionIfFailure { fileHashFn(it.path, fileSystem) }
            is StringContent -> {
                echo("input string: ${it.text}")
                echo("hash: ${stringHashFn(it.text)}")
            }
        }
    }
}
