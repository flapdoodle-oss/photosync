package de.flapdoodle.photosync.filehash

import java.nio.file.Path

interface HashSelector {
  fun hasherFor(path: Path): Hasher<*>

  companion object {
    fun always(hasher: Hasher<*>): HashSelector {
      return object : HashSelector{
        override fun hasherFor(path: Path): Hasher<*> {
          return hasher
        }
      }
    }
  }
}