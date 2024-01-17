package dev.sriniketh

import kotlinx.uuid.SecureRandom
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

/**
 * Creates random UUID.
 *
 * @return Random UUID string.
 */
fun createRandomUUID(): String = UUID.Companion.generateUUID(random = SecureRandom).toString()
