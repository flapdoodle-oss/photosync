package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

interface Hasher<T: Hash<T>> {
  fun hash(path: Path, size: Long, lastModifiedTime: LastModified): T

  fun withMonitor(): Hasher<T> {
    return MonitoringHasher(this)
  }
}