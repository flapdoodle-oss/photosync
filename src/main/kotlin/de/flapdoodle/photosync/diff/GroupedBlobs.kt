package de.flapdoodle.photosync.diff

import de.flapdoodle.photosync.Blob

data class GroupedBlobs(
    val blobs: List<BlobWithMeta>
) {
  init {
    require(blobs.isNotEmpty()) { "blobs is empty" }
  }

  fun any(): Blob {
    return blobs.first().base
  }
}