package de.flapdoodle.io.filetree

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.photosync.add
import java.nio.file.Path

class TreeCollectorAdapter : FileTreeCollector {
  private val root = Dir()
  private var current: Dir = root

  override fun down(path: Path, lastModifiedTime: LastModified): Boolean {
    current = current.down(path)
    return true
  }

  override fun up(path: Path) {
    current = current.up(path)
  }

  override fun add(path: Path, size: Long, lastModifiedTime: LastModified) {
    current.add(path,size,lastModifiedTime)
  }

  override fun addSymlink(path: Path, destination: Path, lastModifiedTime: LastModified) {
    current.addSymlink(path)
  }

  fun asTree(): Tree.Directory {
    require(current === root) {"scanning in progress? $current != $root"}
    return root.asRoot()
  }

  private class Dir(
          private val parent: Dir? = null,
          private val dirPath: Path? = null
  ) {
    private var dirs: Map<Path, Dir> = emptyMap()
    private var files: Map<Path, Pair<Long, LastModified>> = emptyMap()
    private var symlinks: Set<Path> = emptySet()

    fun down(path: Path): Dir {
      val ret = Dir(this, path)
      dirs = dirs add (path to ret)
      return ret
    }

    fun up(path: Path): Dir {
      require(path == dirPath) { "$path != $dirPath" }
      require(parent != null) { "parent is not set" }
      return parent
    }

    fun add(path: Path,size: Long, lastModifiedTime: LastModified) {
      files = files add (path to Pair(size,lastModifiedTime))
    }

    fun addSymlink(path: Path) {
      symlinks = symlinks + path
    }

    fun asRoot(): Tree.Directory {
      require(dirs.size == 1) {"more or less than one entry: $dirs"}
      require(files.isEmpty()) {"files not expected: $files"}

      val entry = dirs.entries.single()

      return Tree.Directory(entry.key, entry.value.asTree())
    }

    private fun asTree(): List<Tree> {
      return dirs.map { Tree.Directory(it.key, it.value.asTree()) } +
          files.map { Tree.File(it.key, it.value.first, it.value.second) } +
          symlinks.map { Tree.SymLink(it) }
    }
  }
}