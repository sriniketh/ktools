import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

fun currentTimeInMillis(clock: Clock = Clock.System): Long = clock.now().toEpochMilliseconds()

fun currentTimeInIso8601(clock: Clock = Clock.System): String = clock.now().toString()

fun timeInMillisToIso8601(timeInMillis: Long): String = Instant.fromEpochMilliseconds(timeInMillis).toString()

@Throws(IllegalArgumentException::class)
fun iso8601ToTimeInMillis(iso8601: String): Long = iso8601.toInstant().toEpochMilliseconds()
