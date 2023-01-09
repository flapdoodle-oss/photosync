package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path

class MonitoringHasher<T : Hash<T>>(
    val delegate: Hasher<T>,
    private val messageOf: (path: Path, size: Long) -> String = { path, size -> "hash $path with $delegate" }
) : Hasher<T> {

    override fun hash(path: Path, size: Long, lastModifiedTime: LastModified): T {
        Monitor.message(messageOf(path, size))
        return delegate.hash(path, size, lastModifiedTime)
    }
}