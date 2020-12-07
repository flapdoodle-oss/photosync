package de.flapdoodle.io.filetree.graph

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

@Deprecated("unused")
sealed class Node(
        open val path: Path
) {

    data class Directory(
            override val path: Path
    ) : Node(path)

    data class File(
            override val path: Path,
            val size: Long,
            val lastModifiedTime: LastModified
    ) : Node(path)

    data class SymLink(
            override val path: Path
    ) : Node(path)
}