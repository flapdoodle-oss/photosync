package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

interface HashCache {
  fun <T: Hash<T>> hash(path: Path, size: Long, lastModifiedTime: LastModified, hasher: Hasher<T>): T
}