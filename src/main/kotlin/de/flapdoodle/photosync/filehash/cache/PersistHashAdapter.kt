package de.flapdoodle.photosync.filehash.cache

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.filehash.Hash

interface PersistHashAdapter<T: Hash<T>> {
  fun toString(hash: T): String
  fun fromString(hash: String): T?
}