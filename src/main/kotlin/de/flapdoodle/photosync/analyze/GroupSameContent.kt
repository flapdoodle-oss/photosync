package de.flapdoodle.photosync.analyze

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.filehash.FullHash
import de.flapdoodle.photosync.filehash.Hasher
import de.flapdoodle.photosync.filehash.QuickHash

class GroupSameContent(
    blobs: Iterable<Blob>,
    hasher: List<Hasher<*>> = listOf(QuickHash.Companion, FullHash.Companion)
) {
  private val sizeCluster: Map<Long, List<Blob>> = blobs.groupBy { it.size }

  init {
    sizeCluster.forEach { size, list ->
      if (list.size>1) {
        println("files with size: $size")
        list.forEach { println("-> $it") }

        groupByHash(list, hasher)
      }
    }
  }

  companion object {
    private fun groupByHash(list: List<Blob>, hasher: List<Hasher<*>>) {
      var collisions = list
      var uniqueBlobs = emptyList<Blob>()

      hasher.forEach {hasher ->
        val groupedByHash = collisions.groupBy { hasher.hash(it.path,it.size) }
        uniqueBlobs = uniqueBlobs + groupedByHash.filter { it.value.size==1 }.values.flatten()

        collisions = groupedByHash.filter { it.value.size > 1 }.values.flatten()

        // TODO .. so funktioniert das noch nicht richtig
        println("collisions left:")
        collisions.forEach {
          println(it)
        }
      }
    }
  }
}