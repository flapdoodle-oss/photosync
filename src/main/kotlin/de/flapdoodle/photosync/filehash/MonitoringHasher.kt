package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path

class MonitoringHasher<T: Hash<T>>(private val delegate: Hasher<T>) : Hasher<T> {

    override fun hash(path: Path, size: Long): T {
        Monitor.message("hash $path with $delegate\n")
        return delegate.hash(path, size)
    }
}