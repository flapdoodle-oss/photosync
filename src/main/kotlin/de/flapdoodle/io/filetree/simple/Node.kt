package de.flapdoodle.io.filetree.simple

import de.flapdoodle.io.filetree.FileTreeCollector
import de.flapdoodle.photosync.LastModified
import de.flapdoodle.types.Either
import java.nio.file.Path

sealed class Node(
    open val name: String,
    open val lastModifiedTime: LastModified
) {
    data class Directory(
        override val name: String,
        override val lastModifiedTime: LastModified,
        val children: List<Node> = emptyList()
    ) : Node(name, lastModifiedTime)

    data class File(
        override val name: String,
        override val lastModifiedTime: LastModified,
        val size: Long
    ) : Node(name, lastModifiedTime)

    data class SymLink(
        override val name: String,
        override val lastModifiedTime: LastModified,
        val destination: Either<Node, Path>
    ) : Node(name, lastModifiedTime)

    companion object {
        fun collector(): NodeTreeCollector {
            return NodeTreeCollector()
        }
    }
}
