package de.flapdoodle.io.tree

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

fun <T> Tree.Directory.mapFiles(mapper: (Tree.File) -> T): List<T> {
    return this.children.flatMap { it ->
        when (it) {
            is Tree.File -> listOf(mapper(it))
            is Tree.Directory -> it.mapFiles(mapper)
            else -> emptyList()
        }
    }
}

sealed class Tree(open val path: Path) {
    data class Directory(override val path: Path, val children: List<Tree> = emptyList()) : Tree(path)
    data class File(
            override val path: Path,
            override val size: Long,
            override val lastModified: LastModified
    ) : Tree(path), IsFile

    data class SymLink(
            override val path: Path,
            val destination: Path,
            val lastModified: LastModified
    ) : Tree(path)
}