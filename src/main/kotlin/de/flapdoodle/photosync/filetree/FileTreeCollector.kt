package de.flapdoodle.photosync.filetree

import java.nio.file.Path

interface FileTreeCollector {
  fun down(path: Path)
  fun up(path: Path)
  fun add(path: Path, symbolicLink: Boolean)

  fun andThen(other: FileTreeCollector): FileTreeCollector {
    val that = this
    return object : FileTreeCollector {
      override fun down(path: Path) {
        that.down(path)
        other.down(path)
      }

      override fun up(path: Path) {
        that.up(path)
        other.up(path)
      }

      override fun add(path: Path, symbolicLink: Boolean) {
        that.add(path,symbolicLink)
        other.add(path,symbolicLink)
      }

    }
  }
}