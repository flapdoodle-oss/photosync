package de.flapdoodle.photosync

import java.nio.file.Path
import java.nio.file.attribute.FileTime

fun Blob.isNewerThan(other: Blob) = lastModifiedTime.toInstant().isAfter(other.lastModifiedTime.toInstant())
fun Blob.isOlderThan(other: Blob) = lastModifiedTime.toInstant().isBefore(other.lastModifiedTime.toInstant())

data class Blob(
    val path: Path,
    val size: Long,
    val lastModifiedTime: FileTime
)