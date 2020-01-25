package de.flapdoodle.photosync.filetree

import de.flapdoodle.photosync.Blob
import de.flapdoodle.photosync.findNotNull
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.regex.Pattern

fun Tree.Directory.mapFiles(mapper: (Tree.File) -> Blob): List<Blob> {
  return this.children.flatMap { it ->
    when (it) {
      is Tree.File -> listOf(mapper(it))
      is Tree.Directory -> it.mapFiles(mapper)
      else -> emptyList()
    }
  }
}

fun Tree.Directory.find(path: Path): Tree.Directory? {
  if (this.path == path) return this
  if (path.startsWith(this.path)) {
    return this.children.findNotNull {
      if (it is Tree.Directory) it.find(path) else null
    }
  }
  return null
}

fun Tree.Directory.get(path: Path): Tree.Directory {
  return find(path) ?: throw IllegalArgumentException("could not find $path in $this")
}

fun Tree.Directory.containsExactly(paths: List<Path>): Boolean {
  if (paths.size == children.size) {
    val files = children.filterIsInstance<Tree.File>()
    if (paths.size == files.size) {
      val filePaths = files.map { it.path }
      if (filePaths.containsAll(paths)) {
        return true
      }
    }
  }
  return false
}

sealed class Tree(
    open val path: Path
) {

  abstract fun filter(filter: (Path) -> Boolean): Tree?

  data class Directory(
      override val path: Path,
      val children: List<Tree>
  ) : Tree(path) {
    override fun filter(filter: (Path) -> Boolean) = if (filter(path)) {
      copy(children = children.mapNotNull { it.filter(filter) })
    } else null

    fun filterChildren(filter: (Path) -> Boolean): Directory = copy(children = children.mapNotNull { it.filter(filter) })
  }

  data class File(
      override val path: Path,
      val size: Long,
      val lastModifiedTime: FileTime
  ) : Tree(path) {
    override fun filter(filter: (Path) -> Boolean) = if (filter(path)) this else null
  }

  data class SymLink(
      override val path: Path
  ) : Tree(path) {
    override fun filter(filter: (Path) -> Boolean) = if (filter(path)) this else null
  }
}