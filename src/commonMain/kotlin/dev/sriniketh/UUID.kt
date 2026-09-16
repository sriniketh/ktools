package dev.sriniketh

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val BITS_PER_BYTE = 8
private const val BYTE_MASK = 0xFF
private const val NIBBLE_BITS = 4
private const val NIBBLE_MASK = 0x0F
private const val HEX_RADIX = 16

private const val UUID_BYTE_LENGTH = 16
private const val RAND_B_BYTE_LENGTH = 8
private const val VERSION_BYTE_INDEX = 6
private const val VARIANT_BYTE_INDEX = 8
private const val TIMESTAMP_BYTE_LENGTH = 6
private const val TIME_LOW_BYTE_LENGTH = 4
private const val TIME_MID_BYTE_LENGTH = 2
private const val TWELVE_BIT_MASK = 0x0FFFL
private const val TWELVE_BIT_WIDTH = 12

private const val VERSION_7_NIBBLE = 0x70
private const val RFC4122_VARIANT_BITS = 0x80
private const val RAND_B_FIRST_BYTE_MASK = 0x3F

private const val VARIANT_TOP_1_BIT_MASK = 0x80
private const val VARIANT_TOP_2_BIT_MASK = 0xC0
private const val VARIANT_TOP_3_BIT_MASK = 0xE0
private const val VARIANT_MICROSOFT_PATTERN = 0xC0

private const val UUID_VERSION_1 = 1
private const val UUID_VERSION_6 = 6
private const val UUID_VERSION_7 = 7

private const val GREGORIAN_TO_UNIX_100NS_OFFSET = 122_192_928_000_000_000L
private const val HUNDRED_NS_PER_MS = 10_000L

private const val URN_PREFIX = "urn:uuid:"

private var lastV7TimestampMillis = -1L
private var lastV7RandA = 0

/**
 * Creates random UUID.
 *
 * @return Random UUID string.
 */
@OptIn(ExperimentalUuidApi::class)
fun createRandomUUID(): String = Uuid.random().toString()

/**
 * Creates a UUIDv7 (RFC 9562) using the given clock for its embedded millisecond timestamp.
 * When called more than once within the same millisecond, the random-A field is incremented
 * so that successive calls still sort lexicographically in creation order.
 *
 * @param[clock] Clock instance to be used. Defaults to instance that queries the
 * operating system as its source of knowledge of time.
 * @return UUIDv7 string.
 */
@OptIn(ExperimentalUuidApi::class)
fun createUuidV7(clock: Clock = Clock.System): String {
    var timestampMillis = clock.now().toEpochMilliseconds()
    val randA = if (timestampMillis > lastV7TimestampMillis) {
        Random.nextInt((TWELVE_BIT_MASK + 1).toInt())
    } else {
        timestampMillis = lastV7TimestampMillis
        (lastV7RandA + 1) and TWELVE_BIT_MASK.toInt()
    }
    lastV7TimestampMillis = timestampMillis
    lastV7RandA = randA

    val randB = Random.nextBytes(RAND_B_BYTE_LENGTH)
    val bytes = ByteArray(UUID_BYTE_LENGTH)
    for (i in 0 until TIMESTAMP_BYTE_LENGTH) {
        bytes[i] = (timestampMillis shr (BITS_PER_BYTE * (TIMESTAMP_BYTE_LENGTH - 1 - i))).toByte()
    }
    bytes[VERSION_BYTE_INDEX] = (VERSION_7_NIBBLE or (randA shr BITS_PER_BYTE)).toByte()
    bytes[VERSION_BYTE_INDEX + 1] = randA.toByte()
    bytes[VARIANT_BYTE_INDEX] = (RFC4122_VARIANT_BITS or (randB[0].toInt() and RAND_B_FIRST_BYTE_MASK)).toByte()
    randB.copyInto(bytes, destinationOffset = VARIANT_BYTE_INDEX + 1, startIndex = 1, endIndex = randB.size)
    return Uuid.fromByteArray(bytes).toString()
}

/**
 * Result of inspecting a UUID: its version and variant, plus, for versions that embed one
 * (v1, v6, v7), the millisecond Unix timestamp.
 */
data class UuidInspection(
    val version: Int,
    val variant: String,
    val timestampMillis: Long?,
    val rawBytesHex: String
)

/**
 * Inspects a UUID string, reporting its version, variant, and, for v1/v6/v7, the embedded
 * timestamp. Accepts uppercase, lowercase, braced (`{...}`), and urn-prefixed (`urn:uuid:...`)
 * input.
 *
 * @param[value] UUID string to inspect.
 * @return Inspection result.
 * @throws IllegalArgumentException if [value] is not a valid UUID.
 */
@OptIn(ExperimentalUuidApi::class)
@Throws(IllegalArgumentException::class)
fun inspectUuid(value: String): UuidInspection {
    var normalized = value.trim()
    if (normalized.startsWith(URN_PREFIX, ignoreCase = true)) {
        normalized = normalized.substring(URN_PREFIX.length)
    }
    if (normalized.startsWith("{") && normalized.endsWith("}")) {
        normalized = normalized.substring(1, normalized.length - 1)
    }

    val bytes = Uuid.parse(normalized).toByteArray()
    val version = (bytes[VERSION_BYTE_INDEX].toInt() shr NIBBLE_BITS) and NIBBLE_MASK
    val variant = classifyVariant(bytes[VARIANT_BYTE_INDEX].toInt())
    val timestampMillis = timestampMillisFor(version, bytes)
    val rawBytesHex = bytes.joinToString("") { byte ->
        (byte.toInt() and BYTE_MASK).toString(HEX_RADIX).padStart(2, '0')
    }
    return UuidInspection(version, variant, timestampMillis, rawBytesHex)
}

private fun classifyVariant(variantByte: Int): String {
    val value = variantByte and BYTE_MASK
    return when {
        (value and VARIANT_TOP_1_BIT_MASK) == 0 -> "NCS"
        (value and VARIANT_TOP_2_BIT_MASK) == RFC4122_VARIANT_BITS -> "RFC 4122"
        (value and VARIANT_TOP_3_BIT_MASK) == VARIANT_MICROSOFT_PATTERN -> "Microsoft"
        else -> "Future"
    }
}

private fun timestampMillisFor(version: Int, bytes: ByteArray): Long? = when (version) {
    UUID_VERSION_7 -> bytes.bigEndianLong(0, TIMESTAMP_BYTE_LENGTH)
    UUID_VERSION_1 -> {
        val timeLow = bytes.bigEndianLong(0, TIME_LOW_BYTE_LENGTH)
        val timeMid = bytes.bigEndianLong(TIME_LOW_BYTE_LENGTH, TIME_MID_BYTE_LENGTH)
        val timeHi = bytes.bigEndianLong(VERSION_BYTE_INDEX, TIME_MID_BYTE_LENGTH) and TWELVE_BIT_MASK
        val timestamp100ns = (timeHi shl (BITS_PER_BYTE * (TIME_LOW_BYTE_LENGTH + TIME_MID_BYTE_LENGTH))) or
            (timeMid shl (BITS_PER_BYTE * TIME_LOW_BYTE_LENGTH)) or
            timeLow
        (timestamp100ns - GREGORIAN_TO_UNIX_100NS_OFFSET) / HUNDRED_NS_PER_MS
    }
    UUID_VERSION_6 -> {
        val timeHigh = bytes.bigEndianLong(0, TIMESTAMP_BYTE_LENGTH)
        val timeLow = bytes.bigEndianLong(VERSION_BYTE_INDEX, TIME_MID_BYTE_LENGTH) and TWELVE_BIT_MASK
        val timestamp100ns = (timeHigh shl TWELVE_BIT_WIDTH) or timeLow
        (timestamp100ns - GREGORIAN_TO_UNIX_100NS_OFFSET) / HUNDRED_NS_PER_MS
    }
    else -> null
}

private fun ByteArray.bigEndianLong(offset: Int, length: Int): Long {
    var result = 0L
    for (i in 0 until length) {
        result = (result shl BITS_PER_BYTE) or (this[offset + i].toLong() and BYTE_MASK.toLong())
    }
    return result
}
