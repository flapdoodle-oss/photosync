package de.flapdoodle.photosync.collector

import java.nio.file.Path
import java.nio.file.attribute.FileTime

class TreeCollector : PathCollector {
  var files: List<Path> = emptyList()
  var dirs: List<Path> = emptyList()

  override fun addDir(path: Path) {
    dirs = dirs + path
  }

  override fun add(path: Path, size: Long, lastModifiedTime: FileTime) {
    files = files + path
  }
}