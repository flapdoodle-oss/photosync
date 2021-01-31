package de.flapdoodle.photosync.paths.meta

import java.nio.file.Path

interface MetafileMatcher {
    fun matches(paths: List<Path>): List<Path>
    fun rename(expectedBase: Path): MetafileRename
}