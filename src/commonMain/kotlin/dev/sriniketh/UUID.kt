package dev.sriniketh

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Creates random UUID.
 *
 * @return Random UUID string.
 */
@OptIn(ExperimentalUuidApi::class)
fun createRandomUUID(): String = Uuid.random().toString()
