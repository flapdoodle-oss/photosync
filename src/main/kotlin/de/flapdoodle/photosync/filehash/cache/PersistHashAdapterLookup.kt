package de.flapdoodle.photosync.filehash.cache

import de.flapdoodle.photosync.filehash.*

interface PersistHashAdapterLookup {
  fun <T : Hash<T>> adapterFor(hasher: Hasher<T>): PersistHashAdapter<T>?

  companion object {
    fun defaultAdapter() = Default()
    
    class Default: PersistHashAdapterLookup {
      override fun <T : Hash<T>> adapterFor(hasher: Hasher<T>): PersistHashAdapter<T>? {
        val unwrappedHasher = if (hasher is MonitoringHasher<T>)
          hasher.delegate
        else
          hasher

        if (unwrappedHasher == FullHash) return PersistFullHash as PersistHashAdapter<T>
        if (unwrappedHasher == SizedQuickHash) return PersistSizedQuickHash as PersistHashAdapter<T>
        return null
      }
    }
  }
}