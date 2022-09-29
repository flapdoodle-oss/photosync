package de.flapdoodle.io.filetree

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

interface FileTreeCollector {
  fun down(path: Path, lastModifiedTime: LastModified): Boolean
  fun up(path: Path)
  fun add(path: Path, size: Long, lastModifiedTime: LastModified)
  fun addSymlink(path: Path, destination: Path, lastModifiedTime: LastModified)

  fun andThen(other: FileTreeCollector): FileTreeCollector {
    val that = this
    return object : FileTreeCollector {
      override fun down(path: Path, lastModifiedTime: LastModified): Boolean {
        return that.down(path, lastModifiedTime) && other.down(path, lastModifiedTime)
      }

      override fun up(path: Path) {
        that.up(path)
        other.up(path)
      }

      override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
        that.add(path, size,lastModifiedTime)
        other.add(path, size, lastModifiedTime)
      }

      override fun addSymlink(path: Path, destination: Path, lastModifiedTime: LastModified) {
        that.addSymlink(path, destination, lastModifiedTime)
        other.addSymlink(path, destination, lastModifiedTime)
      }
    }
  }

  fun withFilter(filter: (Path) -> Boolean): FileTreeCollector {
    val that = this
    return object : FileTreeCollector {
      override fun down(path: Path, lastModifiedTime: LastModified): Boolean {
        return if (filter(path))
          that.down(path, lastModifiedTime)
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

      override fun addSymlink(path: Path, destination: Path, lastModifiedTime: LastModified) {
        if (filter(path)) {
          that.addSymlink(path, destination, lastModifiedTime)
        }
      }
    }
  }

  fun withAbort(abort: (Path) -> Boolean): FileTreeCollector {
    val that = this
    return object  : FileTreeCollector {
      override fun down(path: Path, lastModifiedTime: LastModified): Boolean {
        return !abort(path) && that.down(path, lastModifiedTime)
      }

      override fun up(path: Path) {
        that.up(path)
      }

      override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
        if (!abort(path)) that.add(path, size, lastModifiedTime)
      }

      override fun addSymlink(path: Path, destination: Path, lastModifiedTime: LastModified) {
        if (!abort(path)) that.addSymlink(path, destination, lastModifiedTime)
      }
    }
  }
}