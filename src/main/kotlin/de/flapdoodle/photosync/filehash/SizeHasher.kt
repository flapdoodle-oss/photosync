package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.analyze.GroupSameContent
import java.nio.file.Path

class SizeHasher : Hasher<GroupSameContent.SizeHash> {
  override fun hash(path: Path, size: Long): GroupSameContent.SizeHash {
    return GroupSameContent.SizeHash(size)
  }
}