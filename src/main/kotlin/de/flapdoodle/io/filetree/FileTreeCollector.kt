package de.flapdoodle.io.filetree

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

interface FileTreeCollector {
  fun down(path: Path): Boolean
  fun up(path: Path)
  fun add(path: Path, size: Long, lastModifiedTime: LastModified)
  fun addSymlink(path: Path)

  fun andThen(other: FileTreeCollector): FileTreeCollector {
    val that = this
    return object : FileTreeCollector {
      override fun down(path: Path): Boolean {
        return that.down(path) && other.down(path)
      }

      override fun up(path: Path) {
        that.up(path)
        other.up(path)
      }

      override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
        that.add(path, size,lastModifiedTime)
        other.add(path, size, lastModifiedTime)
      }

      override fun addSymlink(path: Path) {
        that.addSymlink(path)
        other.addSymlink(path)
      }
    }
  }

  fun withFilter(filter: (Path) -> Boolean): FileTreeCollector {
    val that = this
    return object : FileTreeCollector {
      override fun down(path: Path): Boolean {
        return if (filter(path))
          that.down(path)
        else
          false
      }

      override fun up(path: Path) {
        that.up(path)
      }

      override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
        if (filter(path)) {
          that.add(path, size, lastModifiedTime)
        }
      }

      override fun addSymlink(path: Path) {
        if (filter(path)) {
          that.addSymlink(path)
        }
      }
    }
  }

  fun withAbort(abort: (Path) -> Boolean): FileTreeCollector {
    val that = this
    return object  : FileTreeCollector {
      override fun down(path: Path): Boolean {
        return !abort(path) && that.down(path)
      }

      override fun up(path: Path) {
        that.up(path)
      }

      override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
        if (!abort(path)) that.add(path, size, lastModifiedTime)
      }

      override fun addSymlink(path: Path) {
        if (!abort(path)) that.addSymlink(path)
      }
    }
  }
}