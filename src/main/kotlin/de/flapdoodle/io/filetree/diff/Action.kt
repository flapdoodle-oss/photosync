package de.flapdoodle.io.filetree.diff

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

sealed class Action {
    data class CopyFile(val src: Path, val dest: Path, val size: Long): Action()
    data class SetLastModified(val dest: Path, val lastModified: LastModified): Action()

}