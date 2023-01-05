package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

@Deprecated("see SizeHash")
class SizeHasher : Hasher<SizeHash> {
  override fun hash(path: Path, size: Long, lastModifiedTime: LastModified): SizeHash {
    return SizeHash(size)
  }
}