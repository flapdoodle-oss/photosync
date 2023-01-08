package de.flapdoodle.photosync.filehash.cache

import de.flapdoodle.photosync.filehash.FullHash
import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.filehash.SizedQuickHash

interface PersistHashAdapterLookup {
  fun <T : Hash<T>> adapterFor(hasher: Hasher<T>): PersistHashAdapter<T>?

  companion object {
    class Default: PersistHashAdapterLookup {
      override fun <T : Hash<T>> adapterFor(hasher: Hasher<T>): PersistHashAdapter<T>? {
        if (hasher == FullHash) return PersistFullHash as PersistHashAdapter<T>
        if (hasher == SizedQuickHash) return PersistSizedQuickHash as PersistHashAdapter<T>
        return null
      }
    }
  }
}