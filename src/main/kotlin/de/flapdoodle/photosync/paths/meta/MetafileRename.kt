package de.flapdoodle.photosync.paths.meta

import java.nio.file.Path

interface MetafileRename {
    fun rename(path: Path): Path
}