package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent

class Scan(
    val blobs: List<GroupedBlobs>
) {

  companion object {

    fun of(groupedByContent: GroupSameContent, groupMeta: GroupMetaData): Scan {
      val blobs = groupedByContent.uniqueBlobs().map {
        GroupedBlobs(listOf(BlobWithMeta(it, groupMeta.metaBlobs(it))))
      } + groupedByContent.collisions().values.map { list ->
        GroupedBlobs(list.map { BlobWithMeta(it, groupMeta.metaBlobs(it)) })
      }

      return Scan(blobs)
    }
  }

  fun diskSpaceUsed(): Long {
    return blobs.fold(0L) { size, group ->
      size + group.blobs.fold(0L) { subSize, blobWithMeta ->
        subSize + blobWithMeta.base.size + blobWithMeta.meta.fold(0L) { metaSize,meta ->
          metaSize + meta.size
        }
      }
    }
  }
}