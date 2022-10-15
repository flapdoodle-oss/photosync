package de.flapdoodle.io.filetree.diff

import de.flapdoodle.photosync.LastModified
import java.nio.file.Path

sealed class Action {
    data class CopyFile(val src: Path, val dest: Path, val size: Long, val replace: Boolean): Action()
    data class MakeDirectory(val dest: Path): Action()

    data class Remove(val dest: Path): Action()

    data class SetLastModified(val dest: Path, val lastModified: LastModified): Action()
}