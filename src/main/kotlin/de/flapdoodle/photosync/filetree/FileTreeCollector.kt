package de.flapdoodle.photosync.filetree

import java.nio.file.Path
import java.nio.file.attribute.FileTime

interface FileTreeCollector {
  fun down(path: Path)
  fun up(path: Path)
  fun add(path: Path, size: Long, lastModifiedTime: FileTime)
  fun addSymlink(path: Path)

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

      override fun add(path: Path, size: Long, lastModifiedTime: FileTime) {
        that.add(path, size,lastModifiedTime)
        other.add(path, size, lastModifiedTime)
      }

      override fun addSymlink(path: Path) {
        that.addSymlink(path)
        other.addSymlink(path)
      }
    }
  }
}