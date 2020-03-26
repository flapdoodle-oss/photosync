package de.flapdoodle.photosync.filetree

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path
import java.nio.file.attribute.FileTime

class ProgressReportFileTreeCollector : FileTreeCollector {
  override fun down(path: Path) {
    Monitor.message("down $path")
  }

  override fun up(path: Path) {
    Monitor.message("up from $path")
  }

  override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
    Monitor.message("add $path (size=$size)")
  }

  override fun addSymlink(path: Path) {
    Monitor.message("add symlink $path")
  }
}