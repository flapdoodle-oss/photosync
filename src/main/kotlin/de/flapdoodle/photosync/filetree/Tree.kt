package de.flapdoodle.photosync.filetree

import java.nio.file.Path
import java.nio.file.attribute.FileTime

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