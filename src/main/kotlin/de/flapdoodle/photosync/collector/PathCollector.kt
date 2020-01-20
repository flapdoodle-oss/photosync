package de.flapdoodle.photosync.collector

import java.nio.file.Path
import java.nio.file.attribute.FileTime

interface PathCollector {
  fun add(path: Path, size: Long, lastModifiedTime: FileTime)

  fun andThen(other: PathCollector) : PathCollector {
    val that = this
    return object : PathCollector {
      override fun add(path: Path, size: Long, lastModifiedTime: FileTime) {
        that.add(path,size,lastModifiedTime)
        other.add(path,size,lastModifiedTime)
      }
    }
  }
}