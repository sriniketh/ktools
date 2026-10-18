package dev.sriniketh

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.sriniketh.ktools.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.runBlocking
import okio.ByteString.Companion.toByteString
import okio.FileSystem
import okio.IOException
import okio.Path
import kotlinx.io.IOException as NetworkIOException

private const val LATEST_RELEASE_URL = "https://api.github.com/repos/sriniketh/ktools/releases/latest"
private const val CONNECT_TIMEOUT_MILLIS = 15_000L
private const val SOCKET_TIMEOUT_MILLIS = 30_000L

// Curl ignores the socket timeout, so this is its only limit on a stalled download. It fits a ~15 MB slow download.
private const val REQUEST_TIMEOUT_MILLIS = 300_000L

private fun updateHttpClient() = HttpClient {
    install(HttpTimeout) {
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
}

internal class UpdateCommand(
    private val httpClient: () -> HttpClient = ::updateHttpClient,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val currentVersion: String = BuildConfig.VERSION,
    private val executablePath: () -> Path = { FileSystem.SYSTEM.canonicalize(currentExecutablePath()) },
    private val makeExecutable: (Path) -> Unit = ::makeExecutable,
    private val target: ReleaseTarget = currentReleaseTarget,
) : CliktCommand(name = "update") {

    private val checkOnly by option("--check", help = "Only report whether an update is available").flag()

    override fun help(context: Context): String = "Update ktools to the latest release"

    override fun run() {
        try {
            runBlocking { httpClient().use { update(it) } }
        } catch (expectedException: UpdateException) {
            throw PrintMessage(expectedException.message.orEmpty(), statusCode = 1, printError = true)
        } catch (expectedException: IOException) {
            throw PrintMessage("IOException: ${expectedException.message}", statusCode = 1, printError = true)
        }
    }

    private suspend fun update(client: HttpClient) {
        val release = parseGitHubRelease(client.fetch(LATEST_RELEASE_URL).bodyAsText())
        when (val plan = planUpdate(currentVersion, release, target)) {
            is UpdatePlan.UpToDate ->
                echo("ktools ${plan.current} is already up to date (latest release: ${plan.latest})")
            is UpdatePlan.Available if checkOnly -> {
                echo("Update available: ${plan.current} -> ${plan.latest}")
                echo("Run `ktools update` to install it.")
            }
            is UpdatePlan.Available -> install(client, plan)
        }
    }

    private suspend fun install(client: HttpClient, plan: UpdatePlan.Available) {
        val executable = executablePath()
        val sha256Sums = client.fetch(plan.checksumsUrl).bodyAsText()
        val binary = client.fetch(plan.binaryUrl).bodyAsBytes().toByteString()
        verifySha256(binary, sha256Sums, target.assetName)
        try {
            replaceExecutable(fileSystem, executable, binary, makeExecutable)
        } catch (exception: IOException) {
            throw UpdateException(
                "Could not replace $executable: ${exception.message}. " +
                    "Re-run with permission to write to ${executable.parent}.",
                exception
            )
        }
        echo("Updated ktools ${plan.current} -> ${plan.latest}")
    }

    private suspend fun HttpClient.fetch(url: String): HttpResponse {
        val response = try {
            get(url) { header(HttpHeaders.UserAgent, "ktools/$currentVersion") }
        } catch (exception: NetworkIOException) {
            throw UpdateException("Could not reach $url: ${exception.message}", exception)
        } catch (exception: IllegalStateException) {
            throw UpdateException("Could not reach $url: ${exception.message}", exception)
        }
        return response.requireSuccess()
    }

    private fun HttpResponse.requireSuccess(): HttpResponse =
        if (status.isSuccess()) this else throw UpdateException("GET ${request.url} failed: HTTP $status")
}
