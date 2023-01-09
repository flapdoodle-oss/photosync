package de.flapdoodle.photosync.filehash.cache

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filehash.FullHash

object PersistFullHash: PersistHashAdapter<FullHash> {

  override fun key(): String = "FullHash"

  override fun toString(hash: FullHash): String {
    return hash.hash
  }

  override fun fromString(hash: String): FullHash? {
    return FullHash(hash)
  }
}