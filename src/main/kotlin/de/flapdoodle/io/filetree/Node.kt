package de.flapdoodle.io.filetree

import de.flapdoodle.photosync.LastModified
import de.flapdoodle.types.Either
import java.nio.file.Path
import kotlin.io.path.name

sealed class Node(
    open val name: String,
    open val lastModifiedTime: LastModified
) {
    data class Top(
        val path: Path,
        val lastModifiedTime: LastModified,
        val children: List<Node> = emptyList()
    )

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
        val destination: Either<NodeReference, Path>
    ) : Node(name, lastModifiedTime) {

        constructor(name: String, lastModifiedTime: LastModified, destination: NodeReference) :
                this(name, lastModifiedTime, Either.left(destination))

        constructor(name: String, lastModifiedTime: LastModified, destination: Path) :
                this(name, lastModifiedTime, Either.right(destination))
    }

    data class NodeReference(val path: List<String>) {
        constructor(vararg path: String) : this(listOf(*path))

        companion object {
            fun of(path: Path): NodeReference {
                require(!path.isAbsolute) {"path is absolute: $path"}
                val names = path.map { it.name }
                return NodeReference(names)
            }
        }
    }

    companion object {
        fun collector(): NodeTreeCollector {
            return NodeTreeCollector()
        }
    }
}
