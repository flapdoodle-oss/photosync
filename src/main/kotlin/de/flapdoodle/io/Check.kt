package de.flapdoodle.io

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

object Check {
    fun fileSizeIs(size: Long): PathCheck = FileSizeIs(size)
    fun wasLastModifiedAt(lastModified: LastModified): PathCheck = WasLastModifiedAt(lastModified)

    private data class FileSizeIs(val size: Long) : PathCheck {
        override fun check(path: Path) = path.toFile().length() == size
    }

    private data class WasLastModifiedAt(val lastModified: LastModified) : PathCheck {
        override fun check(path: Path) = LastModified.from(path) == lastModified
    }
}