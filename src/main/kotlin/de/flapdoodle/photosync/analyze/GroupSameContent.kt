package de.flapdoodle.photosync.analyze

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.filehash.FullHash
import de.flapdoodle.photosync.filehash.Hash
import de.flapdoodle.photosync.filehash.HashStrategy
import de.flapdoodle.photosync.filehash.QuickHash
import de.flapdoodle.photosync.filehash.SizeHasher

class GroupSameContent(
    blobs: Iterable<Blob>,
    hashStrategy: HashStrategy = HashStrategy { listOf(QuickHash.Companion, FullHash.Companion) }
) {

  private val groupedByHash: Map<Hash<*>,List<Blob>>

  init {
    groupedByHash = HashStrategy.groupBlobs(HashStrategy { listOf(SizeHasher()) + hashStrategy.hasher() }, blobs)
  }

  data class SizeHash(private val size: Long) : Hash<SizeHash>

  fun uniqueBlobs() = groupedByHash.values.filter { it.size==1 }.map { it.single() }
  fun collisions() = groupedByHash.filter { it.value.size>1 }
}