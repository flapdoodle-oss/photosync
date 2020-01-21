package de.flapdoodle.photosync.filetree

import de.flapdoodle.photosync.Blob
import java.nio.file.Path
import java.nio.file.attribute.FileTime

fun Tree.Directory.mapFiles(mapper: (Tree.File) -> Blob): List<Blob> {
  return this.children.flatMap { it ->
    when (it) {
      is Tree.File -> listOf(mapper(it))
      is Tree.Directory -> it.mapFiles(mapper)
      else -> emptyList()
    }
  }
}

sealed class Tree(
    open val path: Path
) {
  data class Directory(
      override val path: Path,
      val children: List<Tree>
  ) : Tree(path)

  data class File(
      override val path: Path,
      val size: Long,
      val lastModifiedTime: FileTime
  ) : Tree(path)

  data class SymLink(
      override val path: Path
  ) : Tree(path)
}