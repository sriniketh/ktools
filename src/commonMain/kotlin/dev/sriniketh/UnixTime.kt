package dev.sriniketh

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Current time in millis.
 *
 * @param[clock] Clock instance to be used. Defaults to instance that queries the operating system as it's source of knowledge of time.
 * @return Current time in millis.
 */
fun currentTimeInMillis(clock: Clock = Clock.System): Long = clock.now().toEpochMilliseconds()

/**
 * Current time in ISO 8601 format.
 *
 * @param[clock] Clock instance to be used. Defaults to instance that queries the operating system as it's source of knowledge of time.
 * @return Current time in ISO 8601 format.
 */
fun currentTimeInIso8601(clock: Clock = Clock.System): String = clock.now().toString()

/**
 * Converts time in millis to  ISO 8601 format.
 *
 * @param[timeInMillis] Time in millis.
 * @return Time in ISO 8601 format.
 */
fun timeInMillisToIso8601(timeInMillis: Long): String = Instant.fromEpochMilliseconds(timeInMillis).toString()

/**
 * Converts time in ISO 8601 format to millis.
 *
 * @param[iso8601] Time in ISO 8601 format.
 * @return Time in millis.
 * @throws IllegalArgumentException
 */
@Throws(IllegalArgumentException::class)
fun iso8601ToTimeInMillis(iso8601: String): Long = Instant.parse(iso8601).toEpochMilliseconds()
