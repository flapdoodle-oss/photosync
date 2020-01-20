package de.flapdoodle.photosync.collector

import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path
import java.nio.file.attribute.FileTime

class ProgressReportPathCollector : PathCollector {
  override fun add(path: Path, size: Long, lastModifiedTime: FileTime) {
    Monitor.message("$path (size=$size)")
  }
}