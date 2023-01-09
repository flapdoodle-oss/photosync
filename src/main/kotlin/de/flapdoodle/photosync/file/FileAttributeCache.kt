package de.flapdoodle.photosync.file

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

interface FileAttributeCache {
  fun get(path: Path, size: Long, lastModifiedTime: LastModified, key: String): ByteArray?
  fun set(path: Path, size: Long, lastModifiedTime: LastModified, key: String, value: ByteArray?)
}