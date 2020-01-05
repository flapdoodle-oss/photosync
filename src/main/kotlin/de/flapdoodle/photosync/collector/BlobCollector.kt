package de.flapdoodle.photosync.collector

import de.flapdoodle.photosync.Blob
import java.nio.file.Path
import java.nio.file.attribute.FileTime

class BlobCollector : PathCollector {
  private var blobs: List<Blob> = emptyList()

  override fun add(path: Path, size: Long, lastModifiedTime: FileTime) {
    blobs = blobs + Blob(path, size, lastModifiedTime)
  }

  fun blobs() = blobs
}