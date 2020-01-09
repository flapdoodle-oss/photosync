package de.flapdoodle.photosync

import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent
import de.flapdoodle.photosync.filehash.Hash

class Scan(
    val groupMeta: GroupMetaData,
    val groupedByContent: GroupSameContent
) {

  private var blobs: List<Blob> = groupedByContent.uniqueBlobs() + firstOf(groupedByContent.collisions())

  companion object {
    private fun firstOf(collisions: Map<Hash<*>, List<Blob>>): List<Blob> {
      return collisions.values.map { it.first() }
    }
  }

  fun blobs() = blobs
}