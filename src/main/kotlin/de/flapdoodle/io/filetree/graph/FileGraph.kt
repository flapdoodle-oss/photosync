package de.flapdoodle.io.filetree.graph

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

sealed class FileGraph(
        open val path: Path
) {

    data class File(
            override val path: Path,
            val size: Long,
            val lastModifiedTime: LastModified
    ) : FileGraph(path)
}