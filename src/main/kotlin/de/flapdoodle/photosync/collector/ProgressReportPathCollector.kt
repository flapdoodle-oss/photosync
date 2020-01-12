package de.flapdoodle.photosync.collector

import java.nio.file.Path
import java.nio.file.attribute.FileTime

class ProgressReportPathCollector : PathCollector {
  override fun add(path: Path, size: Long, lastModifiedTime: FileTime) {
    println("-> $path size=$size\r")
  }
}