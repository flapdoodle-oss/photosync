package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.isMetaOf
import de.flapdoodle.photosync.replaceBase
import java.nio.file.Path

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

  fun replaceBase(newBasePath: Path): BlobWithMeta {
    return BlobWithMeta(
        base = base.copy(path = newBasePath),
        meta = meta.map { it.copy(path = it.path.replaceBase(base.path, newBasePath)) }
    )
  }
}