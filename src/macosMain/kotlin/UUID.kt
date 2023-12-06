import platform.Foundation.NSUUID

actual fun createRandomUUID(): String = NSUUID().UUIDString
