package de.flapdoodle.io.tree

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

sealed class FileTreeEvent {
    data class Down(val path: Path) : FileTreeEvent()
    data class Up(val path: Path) : FileTreeEvent()
    data class SymLink(val path: Path, val destination: Path, val lastModified: LastModified) : FileTreeEvent()
    data class File(val path: Path, val size: Long, val lastModified: LastModified) : FileTreeEvent()
}