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

sealed class Tree(override val path: Path) : HasPath {
    data class Directory(override val path: Path, val children: List<Tree> = emptyList()) : Tree(path) {
        init {
            val pathCollisions = children.groupBy { it.path }.filter { it.value.size > 1 }
            require(pathCollisions.isEmpty()) { "path collisions: $pathCollisions" }
        }

        @Deprecated("use extensionFunction")
        fun childWithPath(childPath: Path): Tree? {
            return children.firstOrNull { it.path == childPath }
        }
    }

    interface IsFileLike

    data class File(
            override val path: Path,
            override val size: Long,
            override val lastModified: LastModified
    ) : Tree(path), IsFile, IsFileLike

    data class SymLink(
            override val path: Path,
            val destination: Path,
            val lastModified: LastModified
    ) : Tree(path), IsFileLike
}