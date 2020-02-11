package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.paths.Meta
import java.nio.file.Path

data class BlobWithMeta(
    val base: Blob,
    val meta: List<Blob> = emptyList()
) {

  init {
    require(meta.all {
      Meta.isMeta(it.path, base.path)
    }) {
      "invalid meta files: $base -> $meta"
    }
  }

  fun replaceBase(newBasePath: Path): BlobWithMeta {
    return BlobWithMeta(
        base = base.copy(path = newBasePath),
        meta = meta.map { it.copy(path = Meta.replaceBase(it.path, base.path, newBasePath)) }
    )
  }
}