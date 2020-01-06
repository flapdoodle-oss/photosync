package de.flapdoodle.photosync.filehash

import java.nio.file.Path

interface Hasher<T: Hash<T>> {
  fun hash(path: Path, size: Long): T
}