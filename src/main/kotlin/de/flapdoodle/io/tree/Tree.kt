package de.flapdoodle.io.tree

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

sealed class Tree(open val path: Path) {
    data class Directory(override val path: Path, val children: List<Tree>) : Tree(path)
    data class File(
            override val path: Path,
            val size: Long,
            val lastModified: LastModified
    ) : Tree(path)

    data class SymLink(
            override val path: Path,
            val destination: Path,
            val lastModified: LastModified
    ) : Tree(path)
}