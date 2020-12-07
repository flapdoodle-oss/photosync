package de.flapdoodle.io.tree

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

interface IsFile {
    val path: Path
    val size: Long
    val lastModified: LastModified
}