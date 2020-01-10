package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent
import de.flapdoodle.photosync.filehash.Hash
import java.nio.file.Path

class Scan(
    val path: Path,
    val groupMeta: GroupMetaData,
    val groupedByContent: GroupSameContent
) {

  companion object {
    private fun firstOf(collisions: Map<Hash<*>, List<Blob>>): List<Blob> {
      return collisions.values.map { it.first() }
    }
  }

  //private var blobs: List<Blob> = groupedByContent.uniqueBlobs() + firstOf(groupedByContent.collisions())
  private val blobs: List<GroupedBlobs>

  init {

    blobs = groupedByContent.uniqueBlobs().map {
      GroupedBlobs(listOf(BlobWithMeta(it, groupMeta.metaBlobs(it))))
    } + groupedByContent.collisions().values.map { list ->
      GroupedBlobs(list.map { BlobWithMeta(it, groupMeta.metaBlobs(it)) })
    }
  }

  fun blobs() = blobs
}