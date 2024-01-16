package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
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

internal class HashCommand(private val fileSystem: FileSystem = FileSystem.SYSTEM) :
    CliktCommand(name = "hash", help = "Get hash value for given file or string") {

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

    override fun run() {
        when (algorithm.lowercase()) {
            "md5" -> {
                when (val it = content) {
                    is FileContent -> printHashForFileOrExceptionIfFailure { fileMD5(it.path, fileSystem) }
                    is StringContent -> {
                        echo("input string: ${it.text}")
                        echo("hash: ${stringMD5(it.text)}")
                    }
                }
            }

            "sha1" -> {
                when (val it = content) {
                    is FileContent -> printHashForFileOrExceptionIfFailure { fileSHA1(it.path, fileSystem) }
                    is StringContent -> {
                        echo("input string: ${it.text}")
                        echo("hash: ${stringSHA1(it.text)}")
                    }
                }
            }

            "sha256" -> {
                when (val it = content) {
                    is FileContent -> printHashForFileOrExceptionIfFailure { fileSHA256(it.path, fileSystem) }
                    is StringContent -> {
                        echo("input string: ${it.text}")
                        echo("hash: ${stringSHA256(it.text)}")
                    }
                }
            }

            "sha512" -> {
                when (val it = content) {
                    is FileContent -> printHashForFileOrExceptionIfFailure { fileSHA512(it.path, fileSystem) }
                    is StringContent -> {
                        echo("input string: ${it.text}")
                        echo("hash: ${stringSHA512(it.text)}")
                    }
                }
            }
        }
    }
}
