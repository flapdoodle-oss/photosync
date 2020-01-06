package de.flapdoodle.photosync.analyze

import de.flapdoodle.photosync.Blob

class GroupSameContent(blobs: Iterable<Blob>) {
  private val sizeCluster: Map<Long, List<Blob>> = blobs.groupBy { it.size }

  init {
    sizeCluster.forEach { size, list ->
      if (list.size>1) {
        println("files with size: $size")
        list.forEach { println("-> $it") }
      }
    }
  }
}