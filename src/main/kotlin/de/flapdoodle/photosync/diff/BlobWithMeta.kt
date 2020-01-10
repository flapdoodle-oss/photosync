package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.isMetaOf

data class BlobWithMeta(
    val base: Blob,
    val meta: List<Blob>
) {
  init {
    require(meta.all {
      it.path.isMetaOf(base.path)
    }) {
      "invalid meta files: $base -> $meta"
    }
  }

}