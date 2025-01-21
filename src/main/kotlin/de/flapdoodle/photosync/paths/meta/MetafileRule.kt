package de.flapdoodle.photosync.paths.meta

import java.nio.file.Path

fun interface MetafileRule {
    fun matcher(path: Path): MetafileMatcher?
}