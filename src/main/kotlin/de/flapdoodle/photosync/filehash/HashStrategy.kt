package de.flapdoodle.photosync.filehash

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.KotlinCompilerFix_SAM_Helper


interface HashStrategy {
  fun hasher(): List<Hasher<*>>

  companion object {

      @KotlinCompilerFix_SAM_Helper
      inline operator fun invoke(
          crossinline delegate: () -> List<Hasher<*>>
      ): HashStrategy {
        return object : HashStrategy {
          override fun hasher(): List<Hasher<*>> {
            return delegate()
          }
        }
      }

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
        var rehashedBlobs: Map<Hash<*>,List<Blob>> = emptyMap()

        groupedBlobs.forEach { hash, list ->
          val parentHash = if (hash != NoHash) hash else null
          rehashedBlobs = if (list.size>1 || !rehashOnCollisionOnly) {
//            println("must rehash ${list.size} entries with $hasher")
            rehashedBlobs + list.groupBy { Hash.prepend(hasher.hash(it.path, it.size),parentHash) }
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