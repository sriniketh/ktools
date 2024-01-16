package dev.sriniketh

internal fun String.removeNewLines() = replace("""\r\n|\n""".toRegex(), "")
