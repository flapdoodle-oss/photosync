package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.analyze.GroupMetaData
import de.flapdoodle.photosync.analyze.GroupSameContent
import java.nio.file.Path

class Scan(
    val path: Path,
    val blobs: List<GroupedBlobs>
) {

  companion object {

    fun of(path: Path, groupedByContent: GroupSameContent, groupMeta: GroupMetaData): Scan {
      val blobs = groupedByContent.uniqueBlobs().map {
        GroupedBlobs(listOf(BlobWithMeta(it, groupMeta.metaBlobs(it))))
      } + groupedByContent.collisions().values.map { list ->
        GroupedBlobs(list.map { BlobWithMeta(it, groupMeta.metaBlobs(it)) })
      }

      return Scan(path, blobs)
    }
  }
}