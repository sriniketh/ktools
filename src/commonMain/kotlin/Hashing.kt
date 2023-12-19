import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath

/**
 * Creates MD5 hash of given file.
 *
 * @param[filepath] Path to file that needs to be hashed.
 * @param[fileSystem] File system to use. Defaults to current process host's file system.
 * @return MD5 hash.
 * @throws IOException
 */
@Throws(IOException::class)
fun fileMD5(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String {
    val byteString = filepath.getByteString(fileSystem)
    return byteString.md5().hex()
}

/**
 * Creates MD5 hash of given string.
 *
 * @param[content] Content to be hashed.
 * @return MD5 hash.
 */
fun stringMD5(content: String): String = content.encodeUtf8().md5().hex()

/**
 * Creates SHA1 hash of given file.
 *
 * @param[filepath] Path to file that needs to be hashed.
 * @param[fileSystem] File system to use. Defaults to current process host's file system.
 * @return SHA1 hash.
 * @throws IOException
 */
@Throws(IOException::class)
fun fileSHA1(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String {
    val byteString = filepath.getByteString(fileSystem)
    return byteString.sha1().hex()
}

/**
 * Creates SHA1 hash of given string.
 *
 * @param[content] Content to be hashed.
 * @return SHA1 hash.
 */
fun stringSHA1(content: String): String = content.encodeUtf8().sha1().hex()

/**
 * Creates SHA256 hash of given file.
 *
 * @param[filepath] Path to file that needs to be hashed.
 * @param[fileSystem] File system to use. Defaults to current process host's file system.
 * @return SHA256 hash.
 * @throws IOException
 */
@Throws(IOException::class)
fun fileSHA256(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String {
    val byteString = filepath.getByteString(fileSystem)
    return byteString.sha256().hex()
}

/**
 * Creates SHA256 hash of given string.
 *
 * @param[content] Content to be hashed.
 * @return SHA256 hash.
 */
fun stringSHA256(content: String): String = content.encodeUtf8().sha256().hex()

/**
 * Creates SHA512 hash of given file.
 *
 * @param[filepath] Path to file that needs to be hashed.
 * @param[fileSystem] File system to use. Defaults to current process host's file system.
 * @return SHA512 hash.
 * @throws IOException
 */
@Throws(IOException::class)
fun fileSHA512(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String {
    val byteString = filepath.getByteString(fileSystem)
    return byteString.sha512().hex()
}

/**
 * Creates SHA512 hash of given string.
 *
 * @param[content] Content to be hashed.
 * @return SHA512 hash.
 */
fun stringSHA512(content: String): String = content.encodeUtf8().sha512().hex()

private fun String.getByteString(fileSystem: FileSystem) =
    fileSystem.read(this.toPath()) { readByteString() }
