package de.flapdoodle.photosync.sync

import de.flapdoodle.photosync.LastModified
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileTime
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalUnit

class TempFile internal constructor(val path: Path) : AutoCloseable {
    private var deleted = false

    fun canSetLastModifiedTime(): Boolean {
        val oldLastModified = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS)
        val newLastModified = FileTime.from(oldLastModified.toInstant().minus(1, ChronoUnit.HOURS))
        Files.setLastModifiedTime(path, newLastModified)
        val readBack = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS)

        require(LastModified.from(readBack) == LastModified.from(newLastModified))
        { "last modified time is different: $readBack != $newLastModified" }
        Files.setLastModifiedTime(path, oldLastModified)

        return true;
    }

    override fun close() {
        if (!deleted) {
            Files.delete(path)
            deleted = true
        }
    }
}