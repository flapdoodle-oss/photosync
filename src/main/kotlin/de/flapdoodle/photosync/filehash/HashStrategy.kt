package de.flapdoodle.photosync.filehash

import de.flapdoodle.io.tree.IsFile
import de.flapdoodle.photosync.Blob


fun interface HashStrategy {
  fun hasher(): List<Hasher<*>>

  companion object {

    fun groupBlobs(
        hasher: Hasher<*>,
        blobs: Iterable<Blob>
    ): Map<Hash<*>, List<Blob>> {
      return groupBlobs(
          hashStrategy = HashStrategy { listOf(hasher) },
          blobs = blobs,
          rehashOnCollisionOnly = false
      )
    }

    fun groupBlobs(
        hashStrategy: HashStrategy,
        blobs: Iterable<Blob>,
        rehashOnCollisionOnly: Boolean = true
    ): Map<Hash<*>, List<Blob>> {
      var groupedBlobs:Map<Hash<*>,List<Blob>> = blobs.groupBy { NoHash }

      hashStrategy.hasher().forEach { hasher ->
        val useHasher = hasher.withMonitor()
        var rehashedBlobs: Map<Hash<*>,List<Blob>> = emptyMap()

        groupedBlobs.forEach { hash, list ->
          val parentHash = if (hash != NoHash) hash else null
          rehashedBlobs = if (list.size>1 || !rehashOnCollisionOnly) {
//            println("must rehash ${list.size} entries with $hasher")
            rehashedBlobs + list.groupBy { Hash.prepend(useHasher.hash(it.path, it.size, it.lastModifiedTime),parentHash) }
          } else {
            rehashedBlobs + (hash to list)
          }
        }

        groupedBlobs = rehashedBlobs
      }

      return groupedBlobs
    }

    fun <T: IsFile> groupBy(
            hashers: List<Hasher<*>>,
            files: Iterable<T>,
            rehashOnCollisionOnly: Boolean = true
    ): Map<Hash<*>, List<T>> {
      var groupedBlobs:Map<Hash<*>,List<T>> = files.groupBy { NoHash }

      hashers.forEach { hasher ->
        val useHasher = hasher.withMonitor()
        var rehashedBlobs: Map<Hash<*>,List<T>> = emptyMap()

        groupedBlobs.forEach { hash, list ->
          val parentHash = if (hash != NoHash) hash else null
          rehashedBlobs = if (list.size>1 || !rehashOnCollisionOnly) {
//            println("must rehash ${list.size} entries with $hasher")
            rehashedBlobs + list.groupBy { Hash.prepend(useHasher.hash(it.path, it.size, it.lastModified),parentHash) }
          } else {
            rehashedBlobs + (hash to list)
          }
        }

        groupedBlobs = rehashedBlobs
      }

      return groupedBlobs
    }
    object NoHash : Hash<NoHash>
  }
}