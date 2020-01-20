package de.flapdoodle.photosync.filetree

import java.nio.file.Path

sealed class Tree(
    open val path: Path
) {
  data class Directory(
      override val path: Path,
      val children: List<Tree>
  ) : Tree(path)

  data class File(
      override val path: Path, val symbolicLink: Boolean = false
  ) : Tree(path)
}