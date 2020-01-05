package de.flapdoodle.photosync

import java.nio.file.Path
import java.nio.file.attribute.FileTime

data class Blob(
    val path: Path,
    val size: Long,
    val lastModifiedTime: FileTime
)