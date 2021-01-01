package de.flapdoodle.io.layouts.metainfo

import de.flapdoodle.photosync.paths.Meta
import java.nio.file.Path

fun interface MatchMetaFile {
    fun basePath(path: Path): Path?
}