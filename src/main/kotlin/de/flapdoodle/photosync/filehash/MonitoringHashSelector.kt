package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

class MonitoringHashSelector(
  private val delegate: HashSelector
) : HashSelector {
  override fun hasherFor(path: Path, size: Long, lastModifiedTime: LastModified): Hasher<*> {
    return MonitoringHasher(delegate.hasherFor(path, size, lastModifiedTime))
  }
}