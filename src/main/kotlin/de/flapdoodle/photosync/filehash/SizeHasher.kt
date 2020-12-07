package de.flapdoodle.photosync.filehash

import java.nio.file.Path

@Deprecated("see SizeHash")
class SizeHasher : Hasher<SizeHash> {
  override fun hash(path: Path, size: Long): SizeHash {
    return SizeHash(size)
  }
}