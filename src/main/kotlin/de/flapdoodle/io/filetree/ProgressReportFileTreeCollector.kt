package de.flapdoodle.io.filetree

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path

class ProgressReportFileTreeCollector : FileTreeCollector {
  override fun down(path: Path): Boolean {
    Monitor.message("down $path")
    return true
  }

  override fun up(path: Path) {
    Monitor.message("up from $path")
  }

  override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
    Monitor.message("add $path (size=$size)")
  }

  override fun addSymlink(path: Path, lastModifiedTime: LastModified) {
    Monitor.message("add symlink $path")
  }
}