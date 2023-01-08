package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

interface HashSelector {
  fun hasherFor(path: Path, size: Long, lastModifiedTime: LastModified): Hasher<*>

  companion object {
    fun always(hasher: Hasher<*>): HashSelector {
      return object : HashSelector{
        override fun hasherFor(path: Path, size: Long, lastModifiedTime: LastModified): Hasher<*> {
          return hasher
        }
      }
    }
  }
}