package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path

class MonitoringHashSelector(
  private val delegate: HashSelector
) : HashSelector {
  override fun hasherFor(path: Path): Hasher<*> {
    return MonitoringHasher(delegate.hasherFor(path))
  }
}