package de.flapdoodle.photosync.filetree

import de.flapdoodle.photosync.progress.Monitor
import java.nio.file.Path

class ProgressReportFileTreeCollector : FileTreeCollector {
  override fun down(path: Path) {
    Monitor.message("down $path")
  }

  override fun up(path: Path) {
    Monitor.message("up from $path")
  }

  override fun add(path: Path, symbolicLink: Boolean) {
    Monitor.message("add $path (symbolic link = $symbolicLink)")
  }
}