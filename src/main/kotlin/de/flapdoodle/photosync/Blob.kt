package de.flapdoodle.photosync

import java.nio.file.Path

//fun Blob.isNewerThan(other: Blob) = lastModifiedTime.toInstant().isAfter(other.lastModifiedTime.toInstant())
//fun Blob.isOlderThan(other: Blob) = lastModifiedTime.toInstant().isBefore(other.lastModifiedTime.toInstant())

data class Blob(
    val path: Path,
    val size: Long,
    val lastModifiedTime: LastModified
)