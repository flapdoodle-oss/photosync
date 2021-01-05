package de.flapdoodle.io.tree

import java.nio.file.Path

fun <T: HasPath> List<T>.childWithPath(childPath: Path): T? {
    return this.firstOrNull { it.path == childPath }
}


interface HasPath {
    val path: Path
}