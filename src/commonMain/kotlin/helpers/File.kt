import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath

/**
 * Helper method to read contents of file and return parsed object of type T.
 *
 * @param[filepath] Path to file that needs to be parsed.
 * @param[fileSystem] File system to use. Defaults to current process host's file system.
 * @return Parsed object of type T.
 * @throws IOException
 * @throws SerializationException
 * @throws IllegalArgumentException
 */
@Throws(IOException::class, SerializationException::class, IllegalArgumentException::class)
inline fun <reified T> parseFile(
    filepath: String,
    ignoreUnknownKeys: Boolean = true,
    fileSystem: FileSystem = FileSystem.SYSTEM
): T {
    val path = filepath.toPath()
    val jsonString = fileSystem.read(path) { this.readUtf8() }
    val json = Json { this.ignoreUnknownKeys = ignoreUnknownKeys }
    return json.decodeFromString<T>(jsonString)
}
