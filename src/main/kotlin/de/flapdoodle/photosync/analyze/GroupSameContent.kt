package de.flapdoodle.photosync.analyze

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.filehash.*

class GroupSameContent(
    blobs: Iterable<Blob>,
    hashStrategy: HashStrategy = HashStrategy { listOf(QuickHash.Companion, FullHash.Companion) }
) {

  private val groupedByHash: Map<Hash<*>,List<Blob>>

  init {
    groupedByHash = HashStrategy.groupBlobs(HashStrategy { listOf(SizeHash) + hashStrategy.hasher() }, blobs)
  }

  fun uniqueBlobs() = groupedByHash.values.filter { it.size==1 }.map { it.single() }
  fun collisions() = groupedByHash.filter { it.value.size>1 }
}