package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path

interface Hasher<T: Hash<T>> {
  fun hash(path: Path, size: Long): T

  fun withMonitor(): Hasher<T> {
    val that = this
    return object: Hasher<T> {
      override fun hash(path: Path, size: Long): T {
        Monitor.message("$path")
        return that.hash(path,size)
      }
    }
  }
}