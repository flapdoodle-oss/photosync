package de.flapdoodle.photosync.filehash.cache

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.file.FileAttributeCache
import de.flapdoodle.photosync.filehash.HashSelector
import de.flapdoodle.photosync.filehash.Hasher
import java.nio.file.Path

class PersistentHashSelector(
  private val cache: FileAttributeCache,
  private val minPersistableSize: Long = 1024L,
): HashSelector {
  override fun hasherFor(path: Path, size: Long, lastModifiedTime: LastModified): Hasher<*> {
    TODO("Not yet implemented")
  }
}