import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.IOException
import okio.Path.Companion.toPath

@Throws(IOException::class)
fun fileMD5(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String {
    val byteString = filepath.getByteString(fileSystem)
    return byteString.md5().hex()
}

fun stringMD5(content: String): String = content.encodeUtf8().md5().hex()

@Throws(IOException::class)
fun fileSHA1(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String {
    val byteString = filepath.getByteString(fileSystem)
    return byteString.sha1().hex()
}

fun stringSHA1(content: String): String = content.encodeUtf8().sha1().hex()

@Throws(IOException::class)
fun fileSHA256(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String {
    val byteString = filepath.getByteString(fileSystem)
    return byteString.sha256().hex()
}

fun stringSHA256(content: String): String = content.encodeUtf8().sha256().hex()

@Throws(IOException::class)
fun fileSHA512(filepath: String, fileSystem: FileSystem = FileSystem.SYSTEM): String {
    val byteString = filepath.getByteString(fileSystem)
    return byteString.sha512().hex()
}

fun stringSHA512(content: String): String = content.encodeUtf8().sha512().hex()

private fun String.getByteString(fileSystem: FileSystem) =
    fileSystem.read(this.toPath()) { readByteString() }
