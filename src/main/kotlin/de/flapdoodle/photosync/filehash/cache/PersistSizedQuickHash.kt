package de.flapdoodle.photosync.filehash.cache

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filehash.SizedQuickHash

object PersistSizedQuickHash: PersistHashAdapter<SizedQuickHash> {

  override fun key(): String = "SizeQuickHash"

  override fun toString(hash: SizedQuickHash): String {
    return "${SizedQuickHash.blockSize()}:${hash.startHash}:${hash.size}:${hash.endHash}"
  }

  override fun fromString(hash: String): SizedQuickHash? {
    val parts = hash.split(':')
    if (parts.size==4) {
      val blockSize = Integer.parseInt(parts[0])
      if (blockSize==SizedQuickHash.blockSize()) {
        return SizedQuickHash(parts[1], parts[2].toLong(), parts[3])
      }
    }
    return null
  }
}